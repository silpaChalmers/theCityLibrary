package com.ibizabroker.lms.configuration;

import com.github.stefanbirkner.systemlambda.SystemLambda;
import com.ibizabroker.lms.service.JwtService;
import com.ibizabroker.lms.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtRequestFilterTest {

    @InjectMocks
    private JwtRequestFilter jwtRequestFilter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // 确保每个测试前清空 SecurityContext
        SecurityContextHolder.clearContext();
    }

    /**
     * 1. 测试：有效 Token，验证 setDetails(...) 也被调用
     */
    @Test
    void testDoFilterInternal_ValidToken() throws Exception {
        String token = "validToken";
        String username = "testUser";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.getUsernameFromToken(token)).thenReturn(username);
        when(jwtService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(true);

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // 验证 SecurityContext 中的 Authentication 已经被设置
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        // 验证 setDetails(...) 是否被真正执行：details 不应为 null
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication.getDetails(), "Authentication details should not be null");

        verify(filterChain, times(1)).doFilter(request, response);
    }

    /**
     * 2. 测试：请求头中没有 Authorization 字段
     *    => 走 else 分支，输出 "JWT token does not start with Bearer"
     */
    @Test
    void testDoFilterInternal_MissingAuthorizationHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        // 捕获 System.out.println 输出
        String consoleOutput = SystemLambda.tapSystemOut(() ->
                jwtRequestFilter.doFilterInternal(request, response, filterChain)
        );

        assertTrue(consoleOutput.contains("JWT token does not start with Bearer"),
                "Missing Authorization header should log a message");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    /**
     * 3. 测试：Authorization 不以 "Bearer " 开头
     *    => 同样走 else 分支，输出 "JWT token does not start with Bearer"
     */
    @Test
    void testDoFilterInternal_InvalidTokenPrefix() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("InvalidPrefix");

        String consoleOutput = SystemLambda.tapSystemOut(() ->
                jwtRequestFilter.doFilterInternal(request, response, filterChain)
        );

        assertTrue(consoleOutput.contains("JWT token does not start with Bearer"),
                "Invalid prefix should log a message");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    /**
     * 4. 测试：JWT 无法正确解析（抛出 IllegalArgumentException）
     *    => 捕获 System.out.println("Unable to get JWT Token")
     */
    @Test
    void testDoFilterInternal_IllegalArgumentException() throws Exception {
        String token = "illegalToken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.getUsernameFromToken(token)).thenThrow(new IllegalArgumentException("Unable to get JWT Token"));

        String consoleOutput = SystemLambda.tapSystemOut(() ->
                jwtRequestFilter.doFilterInternal(request, response, filterChain)
        );

        assertTrue(consoleOutput.contains("Unable to get JWT Token"),
                "IllegalArgumentException should log a message");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    /**
     * 5. 测试：JWT 已经过期（抛出 ExpiredJwtException）
     *    => 捕获 System.out.println("JWT Token has expired")
     */
    @Test
    void testDoFilterInternal_ExpiredToken() throws Exception {
        String token = "expiredToken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        // 构造一个 ExpiredJwtException
        ExpiredJwtException expiredEx =
                new ExpiredJwtException(null, null, "JWT Token has expired");
        when(jwtUtil.getUsernameFromToken(token)).thenThrow(expiredEx);

        String consoleOutput = SystemLambda.tapSystemOut(() ->
                jwtRequestFilter.doFilterInternal(request, response, filterChain)
        );

        assertTrue(consoleOutput.contains("JWT Token has expired"),
                "Expired token should log a message");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

}
