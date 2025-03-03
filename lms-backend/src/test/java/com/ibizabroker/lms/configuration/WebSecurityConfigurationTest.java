package com.ibizabroker.lms.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 测试 WebSecurityConfiguration 中 authenticationManagerBean() 与 passwordEncoder() 方法，
 * 确保返回值不为 null，从而杀死对应的变异。
 *
 * 处理方式：
 * 1. 通过 @TestConfiguration + @Import 提供 DummyUserDetailsService 供安全配置使用；
 * 2. 使用 @MockBean 提供 JwtAuthenticationEntryPoint 与 JwtRequestFilter 的模拟对象；
 * 3. 在测试中实际调用 authenticationManager 与 passwordEncoder 的方法，让 null 引发异常。
 */
@SpringBootTest
@Import(WebSecurityConfigurationTest.DummyUserDetailsService.class)
public class WebSecurityConfigurationTest {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    /**
     * 测试 authenticationManagerBean() 返回的 AuthenticationManager 不为 null，并实际调用其 authenticate()。
     * 如果被变异成 null，则此处会抛出 NullPointerException，从而杀死变异。
     */
    @Test
    public void testAuthenticationManagerBean() throws Exception {
        assertNotNull(authenticationManager, "AuthenticationManager should not be null");

        // 额外使用它做一次虚拟认证，若变异为 null，就会抛 NPE
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken("user", "password")
        );
    }

    /**
     * 测试 passwordEncoder() 返回的 PasswordEncoder 不为 null，且为 BCryptPasswordEncoder 实例（类名中包含“BCryptPasswordEncoder”），
     * 并实际调用 encode()。如果被变异成 null，则此处会抛 NullPointerException，从而杀死变异。
     */
    @Test
    public void testPasswordEncoder() {
        assertNotNull(passwordEncoder, "PasswordEncoder should not be null");
        assertTrue(passwordEncoder.getClass().getSimpleName().contains("BCryptPasswordEncoder"),
            "PasswordEncoder should be BCryptPasswordEncoder");
        // 额外调用 encode(...)，若变异为 null，就会抛 NPE
        String encoded = passwordEncoder.encode("testValue");
        assertNotNull(encoded, "Encoded result should not be null");
    }

    /**
     * Dummy UserDetailsService 实现，用于构建 AuthenticationManager。
     */
    @TestConfiguration
    @Primary
    static class DummyUserDetailsService implements UserDetailsService {
        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            // 使用和测试里相同的 "user" 作为用户名
            // 这里将密码加密成 BCrypt 后存储，保证与 WebSecurityConfiguration 的 BCryptPasswordEncoder 相匹配
            String hashedPassword = new BCryptPasswordEncoder().encode("password");
            return new org.springframework.security.core.userdetails.User(
                "user",
                hashedPassword,
                new ArrayList<>()
            );
        }
    }

}
