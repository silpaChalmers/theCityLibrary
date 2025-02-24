package com.ibizabroker.lms.controller;

import com.ibizabroker.lms.configuration.JwtAuthenticationEntryPoint;
import com.ibizabroker.lms.configuration.JwtRequestFilter;
import com.ibizabroker.lms.configuration.WebSecurityConfiguration;
import com.ibizabroker.lms.entity.JwtResponse;
import com.ibizabroker.lms.entity.Users;
import com.ibizabroker.lms.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = JwtController.class,
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {
                WebSecurityConfiguration.class,
                JwtRequestFilter.class,
                JwtAuthenticationEntryPoint.class
            }
        )
    }
)
@AutoConfigureMockMvc(addFilters = false)
class JwtControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    /**
     * 测试：成功生成 JWT
     */
    @Test
    void testCreateJwtToken_Success() throws Exception {
        String requestJson = "{\"username\":\"testUser\",\"password\":\"testPassword\"}";

        Users mockUser = new Users();
        mockUser.setUsername("testUser");
        JwtResponse mockResponse = new JwtResponse(mockUser, "fakeJwtToken");

        when(jwtService.createJwtToken(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("testUser"))
                .andExpect(jsonPath("$.jwtToken").value("fakeJwtToken"));
    }

    /**
     * 测试：生成 JWT 时发生异常（模拟无效用户名/密码）
     * 无自定义异常处理 => 默认返回 500
     */
    @Test
    void testCreateJwtToken_InvalidCredentials() throws Exception {
        String requestJson = "{\"username\":\"badUser\",\"password\":\"badPassword\"}";

        when(jwtService.createJwtToken(any()))
                .thenThrow(new RuntimeException("INVALID_CREDENTIALS"));

        // 用 assertThrows 捕获抛出的 NestedServletException
        NestedServletException ex = assertThrows(NestedServletException.class, () -> {
            // 这里的 perform 会抛异常
            mockMvc.perform(post("/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    // 不要再 andExpect(status().isInternalServerError())
                    // 因为请求不会到达返回 HTTP 响应那一步
                    .andReturn();
        });

        // 验证内部 cause 是 RuntimeException
        Throwable cause = ex.getCause();
        if (cause == null) {
            cause = ex; // 万一没有内层 cause，直接用 ex
        }
        // 断言类型和消息
        org.junit.jupiter.api.Assertions.assertTrue(cause instanceof RuntimeException);
        org.junit.jupiter.api.Assertions.assertEquals("INVALID_CREDENTIALS", cause.getMessage());
    }
}
