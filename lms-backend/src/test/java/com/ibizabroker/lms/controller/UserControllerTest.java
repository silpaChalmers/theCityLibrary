package com.ibizabroker.lms.controller;

import com.ibizabroker.lms.configuration.JwtAuthenticationEntryPoint;
import com.ibizabroker.lms.configuration.JwtRequestFilter;
import com.ibizabroker.lms.configuration.WebSecurityConfiguration;
import com.ibizabroker.lms.dao.UsersRepository;
import com.ibizabroker.lms.entity.Users;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = UserController.class,
    // ① 排除自定义安全配置和过滤器，防止加载 JWT 相关 Bean
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
// ② 禁用所有过滤器（包括 Spring Security）以免被拦截
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private UsersRepository usersRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    void testRegisterNewUser() throws Exception {
        // 模拟一个用户
        Users user = new Users();
        user.setUsername("testuser");
        user.setPassword("rawpassword");

        // 当加密 rawpassword 时，返回 encodedpassword
        when(passwordEncoder.encode("rawpassword")).thenReturn("encodedpassword");
        // 当保存用户时，返回同一个 user
        when(usersRepository.save(any(Users.class))).thenReturn(user);

        // 构造请求 JSON
        String userJson = "{\"username\":\"testuser\", \"password\":\"rawpassword\"}";

        // 发送 POST 请求，断言结果
        mockMvc.perform(post("/user/adduser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.password").value("encodedpassword"));
    }
}
