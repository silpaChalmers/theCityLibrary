package com.ibizabroker.lms.service;

import com.ibizabroker.lms.dao.UsersRepository;
import com.ibizabroker.lms.entity.JwtRequest;
import com.ibizabroker.lms.entity.JwtResponse;
import com.ibizabroker.lms.entity.Role;
import com.ibizabroker.lms.entity.Users;
import com.ibizabroker.lms.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 使用 @SpringBootTest 测试 JwtService
 */
@SpringBootTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UsersRepository userDao;

    /**
     * 测试：成功生成 JWT
     */
    @Test
    void testCreateJwtToken_Success() throws Exception {
        // 构造请求
        JwtRequest jwtRequest = new JwtRequest();
        jwtRequest.setUserName("testUser");
        jwtRequest.setUserPassword("testPassword");

        // 构造模拟用户
        Users mockUser = new Users();
        mockUser.setUsername("testUser");
        mockUser.setPassword("testPassword");
        mockUser.setRole(Collections.emptySet()); // 这里仍然使用空角色

        when(userDao.findByUsername("testUser")).thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("fakeJwtToken");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("testUser", "testPassword", Collections.emptyList()));

        JwtResponse jwtResponse = jwtService.createJwtToken(jwtRequest);

        assertNotNull(jwtResponse);
        assertEquals("fakeJwtToken", jwtResponse.getJwtToken());
        assertNotNull(jwtResponse.getUser());
        assertEquals("testUser", jwtResponse.getUser().getUsername());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateToken(any(UserDetails.class));
        verify(userDao, times(2)).findByUsername("testUser");
    }

    /**
     * 测试：生成 JWT 时因认证失败抛出异常（例如：无效用户名/密码）
     */
    @Test
    void testCreateJwtToken_InvalidCredentials() throws Exception {
        JwtRequest jwtRequest = new JwtRequest();
        jwtRequest.setUserName("badUser");
        jwtRequest.setUserPassword("badPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("INVALID_CREDENTIALS"));

        Exception exception = assertThrows(Exception.class, () -> jwtService.createJwtToken(jwtRequest));
        assertTrue(exception.getMessage().contains("INVALID_CREDENTIALS"));

        verify(userDao, never()).findByUsername(anyString());
        verify(jwtUtil, never()).generateToken(any());
    }

    /**
     * 测试：loadUserByUsername 成功查找到用户（无角色）
     */
    @Test
    void testLoadUserByUsername_UserFound() {
        Users mockUser = new Users();
        mockUser.setUsername("existingUser");
        mockUser.setPassword("password");
        mockUser.setRole(Collections.emptySet());

        when(userDao.findByUsername("existingUser")).thenReturn(Optional.of(mockUser));

        UserDetails userDetails = jwtService.loadUserByUsername("existingUser");
        assertNotNull(userDetails);
        assertEquals("existingUser", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        // Authorities 为空
        assertTrue(userDetails.getAuthorities().isEmpty());
    }

    /**
     * 测试：loadUserByUsername 成功查找到用户（带角色）
     * 让 role 集合里至少有一个角色，以覆盖 getAuthority(user) 的 forEach 逻辑
     */
    @Test
    void testLoadUserByUsername_UserFoundWithRole() {
        // 构造一个带角色的用户
        Users mockUser = new Users();
        mockUser.setUsername("userWithRole");
        mockUser.setPassword("password");

        // 创建一个 Role 对象，并设置 roleName
        Role adminRole = new Role();
        adminRole.setRoleName("ADMIN");

        // 将角色放到集合里
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);

        // 将角色集合赋给用户
        mockUser.setRole(roles);

        when(userDao.findByUsername("userWithRole")).thenReturn(Optional.of(mockUser));

        // 调用
        UserDetails userDetails = jwtService.loadUserByUsername("userWithRole");

        // 断言
        assertNotNull(userDetails);
        assertEquals("userWithRole", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());

        // 检查是否含有 ROLE_ADMIN
        assertTrue(
            userDetails.getAuthorities().stream()
                       .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")),
            "Expected to find authority ROLE_ADMIN"
        );
    }

    /**
     * 测试：loadUserByUsername 未查找到用户时抛出异常
     * 注意：由于 JwtService 内部直接调用 Optional.get()，因此会抛出 NoSuchElementException
     */
    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(userDao.findByUsername("nonexistent")).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> jwtService.loadUserByUsername("nonexistent"));
    }
}
