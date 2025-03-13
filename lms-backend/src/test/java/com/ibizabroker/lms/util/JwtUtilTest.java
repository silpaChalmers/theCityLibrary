package com.ibizabroker.lms.util;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.Claims;

/**
 * 使用 @SpringBootTest 测试 JwtUtil 工具类
 */
@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 测试：生成 token 后能够正确提取用户名
     */
    @Test
    void testGenerateTokenAndExtractUsername() {
        UserDetails user = new User("testUser", "password", new ArrayList<>());
        String token = jwtUtil.generateToken(user);
        assertNotNull(token, "生成的 token 不应为 null");

        String extractedUsername = jwtUtil.getUsernameFromToken(token);
        assertEquals("testUser", extractedUsername, "提取的用户名应与原始用户名一致");
    }

    /**
     * 测试：使用正确的 UserDetails 校验 token 应返回 true
     */
    @Test
    void testValidateToken_Success() {
        UserDetails user = new User("testUser", "password", new ArrayList<>());
        String token = jwtUtil.generateToken(user);
        assertTrue(jwtUtil.validateToken(token, user), "对于相同用户，校验 token 应返回 true");
    }

    /**
     * 测试：使用不同的 UserDetails 校验 token 应返回 false
     */
    @Test
    void testValidateToken_Failure() {
        UserDetails user = new User("testUser", "password", new ArrayList<>());
        String token = jwtUtil.generateToken(user);
        // 构造一个用户名不同的用户
        UserDetails otherUser = new User("otherUser", "password", new ArrayList<>());
        assertFalse(jwtUtil.validateToken(token, otherUser), "不同用户校验 token 应返回 false");
    }

    /**
     * 测试：获取 token 的过期时间，并验证其在当前时间之后
     */
    @Test
    void testGetExpirationDateFromToken() {
        UserDetails user = new User("testUser", "password", new ArrayList<>());
        String token = jwtUtil.generateToken(user);
        Date expirationDate = jwtUtil.getExpirationDateFromToken(token);
        assertNotNull(expirationDate, "过期时间不应为 null");
        assertTrue(expirationDate.after(new Date()), "过期时间应在当前时间之后");
    }

    /**
     * 测试：通过 getClaimFromToken 方法获取 token 中的各个声明信息
     */
    @Test
    void testGetClaimFromToken() {
        UserDetails user = new User("testUser", "password", new ArrayList<>());
        String token = jwtUtil.generateToken(user);

        // 获取过期时间声明
        Date expiration = jwtUtil.getClaimFromToken(token, Claims::getExpiration);
        assertNotNull(expiration, "通过 getClaimFromToken 获取的过期时间不应为 null");

        // 获取主题（用户名）声明
        String subject = jwtUtil.getClaimFromToken(token, Claims::getSubject);
        assertEquals("testUser", subject, "主题应等于用户名");
    }
    
}
