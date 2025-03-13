package com.ibizabroker.lms.controller;

import com.ibizabroker.lms.configuration.JwtAuthenticationEntryPoint;
import com.ibizabroker.lms.configuration.JwtRequestFilter;
import com.ibizabroker.lms.configuration.WebSecurityConfiguration;
import com.ibizabroker.lms.dao.UsersRepository;
import com.ibizabroker.lms.entity.Users;
import com.ibizabroker.lms.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = AdminController.class,
    // 排除自定义安全配置 & 过滤器，避免加载 JWT 相关 Bean
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
// 禁用所有过滤器（包括 Spring Security），防止 403 等安全拦截
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsersRepository usersRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    /**
     * 测试新增用户（POST /admin/users）
     */
    @Test
    void testAddUserByAdmin() throws Exception {
        // 构造模拟用户
        Users user = new Users();
        user.setUsername("adminuser");
        user.setPassword("rawpass");

        // 模拟密码加密
        when(passwordEncoder.encode("rawpass")).thenReturn("encodedpass");
        // 模拟存储用户时，返回传入的 user（也可以用 thenAnswer(...)）
        when(usersRepository.save(any(Users.class))).thenReturn(user);

        // 构造请求 JSON
        String json = "{\"username\":\"adminuser\", \"password\":\"rawpass\"}";

        // 执行 POST 请求并断言结果
        mockMvc.perform(post("/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("adminuser"))
                .andExpect(jsonPath("$.password").value("encodedpass"));
    }

    /**
     * 测试获取所有用户（GET /admin/users）
     */
    @Test
    void testGetAllUsers() throws Exception {
        Users user1 = new Users();
        user1.setUsername("user1");

        Users user2 = new Users();
        user2.setUsername("user2");

        // 模拟返回两条记录
        when(usersRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].username").value("user2"));
    }

    /**
     * 测试按 ID 获取用户（GET /admin/users/{id}）- 找到用户
     */
    @Test
    void testGetUserById_Found() throws Exception {
        Users user = new Users();
        user.setUsername("foundUser");

        // 模拟查到用户
        when(usersRepository.findById(1)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/admin/users/1"))
                .andExpect(status().isOk())
                //.andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("foundUser"));
    }

    /**
     * 测试按 ID 获取用户（GET /admin/users/{id}）- 未找到用户
     */
    @Test
    void testGetUserById_NotFound() throws Exception {
        // 模拟查不到用户
        when(usersRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/users/999"))
                // 如果 NotFoundException 有全局异常处理映射为 404，则此处断言 404
                .andExpect(status().isNotFound());
    }

    /**
     * 测试更新用户（PUT /admin/users/{id}）
     */
    @Test
    void testUpdateUser() throws Exception {
        Users existingUser = new Users();
        existingUser.setUsername("oldUsername");
        existingUser.setName("oldName");

        // 模拟找到旧用户
        when(usersRepository.findById(15)).thenReturn(Optional.of(existingUser));
        // 模拟更新后直接返回更新的用户
        when(usersRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 新的请求体 JSON
        String json = "{\"username\":\"newUsername\", \"name\":\"newName\"}";

        mockMvc.perform(put("/admin/users/15")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                //.andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.username").value("newUsername"))
                .andExpect(jsonPath("$.name").value("newName"));
    }

     /**
     * 测试：PUT /admin/users/{id}
     * 更新用户时找不到
     */
    @Test
    void testUpdateUser_NotFound() throws Exception {
        // 模拟找不到用户
        when(usersRepository.findById(999)).thenReturn(Optional.empty());

        String json = "{\"username\":\"newUsername\", \"name\":\"newName\"}";

        mockMvc.perform(put("/admin/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }
}
