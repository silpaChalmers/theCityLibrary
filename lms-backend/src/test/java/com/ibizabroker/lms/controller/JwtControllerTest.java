package com.ibizabroker.lms.controller;

import com.ibizabroker.lms.entity.JwtRequest;
import com.ibizabroker.lms.entity.JwtResponse;
import com.ibizabroker.lms.entity.Users;
import com.ibizabroker.lms.service.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 使用 @SpringBootTest + @AutoConfigureMockMvc 测试 JwtController
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class JwtControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    // 如果要从容器里获取 CorsConfiguration / WebSecurityConfiguration，需要注入 ApplicationContext
    @Autowired
    private ApplicationContext context;

    /**
     * 测试：成功生成 JWT
     */
    @Test
    void testCreateJwtToken_Success() throws Exception {
        // 构造请求 JSON
        String requestJson = "{\"username\":\"testUser\",\"password\":\"testPassword\"}";

        // 模拟 Service 返回的结果
        Users mockUser = new Users();
        mockUser.setUsername("testUser");
        JwtResponse mockResponse = new JwtResponse(mockUser, "fakeJwtToken");

        when(jwtService.createJwtToken(any())).thenReturn(mockResponse);

        // 执行 POST /authenticate
        mockMvc.perform(post("/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("testUser"))
                .andExpect(jsonPath("$.jwtToken").value("fakeJwtToken"));

        // 验证 Controller 确实调用了 jwtService.createJwtToken(...)，并捕获传入的 JwtRequest
        ArgumentCaptor<JwtRequest> captor = ArgumentCaptor.forClass(JwtRequest.class);
        verify(jwtService, times(1)).createJwtToken(captor.capture());

        JwtRequest capturedRequest = captor.getValue();
        assertEquals("testUser", capturedRequest.getUsername());
        assertEquals("testPassword", capturedRequest.getPassword());
    }

    /**
     * 测试：生成 JWT 时发生异常（模拟无效用户名/密码）
     * 无自定义异常处理 => 默认返回 500
     */
    @Test
    void testCreateJwtToken_InvalidCredentials() throws Exception {
        String requestJson = "{\"username\":\"badUser\",\"password\":\"badPassword\"}";

        // 当 jwtService.createJwtToken 被调用时抛出异常
        when(jwtService.createJwtToken(any()))
                .thenThrow(new RuntimeException("INVALID_CREDENTIALS"));

        // 用 assertThrows 捕获抛出的 NestedServletException
        NestedServletException ex = assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(post("/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andReturn();
        });

        // 验证内部 cause 是 RuntimeException 且消息正确
        Throwable cause = ex.getCause() == null ? ex : ex.getCause();
        assertTrue(cause instanceof RuntimeException);
        assertEquals("INVALID_CREDENTIALS", cause.getMessage());
    }

    // ========================= 新增以下两个测试方法 =========================

    /**
     * 1. 测试 CorsConfiguration::corsConfigurer() 不为 null
     *    用于杀死“将返回值替换为 null”的存活变异。
     */
    @Test
    void testCorsConfigurerBeanNotNull() {
        // 假设你的 CorsConfiguration 类在容器里
        com.ibizabroker.lms.configuration.CorsConfiguration corsConfiguration
                = context.getBean(com.ibizabroker.lms.configuration.CorsConfiguration.class);

        assertNotNull(corsConfiguration.corsConfigurer(),
                "corsConfigurer() 应该返回非 null 的 WebMvcConfigurer");
    }

    /**
     * 2. 测试 WebSecurityConfiguration::authenticationManagerBean() 不为 null
     *    用于杀死“将返回值替换为 null”的存活变异。
     *    注意：如果 WebSecurityConfiguration 不在容器里，需要相应修改获取方式
     */
    @Test
    void testAuthenticationManagerBeanNotNull() throws Exception {
        // 假设你的 WebSecurityConfiguration 类在容器里
        com.ibizabroker.lms.configuration.WebSecurityConfiguration webSecurityConfiguration
                = context.getBean(com.ibizabroker.lms.configuration.WebSecurityConfiguration.class);

        assertNotNull(webSecurityConfiguration.authenticationManagerBean(),
                "authenticationManagerBean() 应该返回非 null 的 AuthenticationManager");
    }
}
