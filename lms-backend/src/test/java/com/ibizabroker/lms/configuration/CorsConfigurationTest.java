package com.ibizabroker.lms.configuration;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 测试全局 CorsConfiguration 是否正确生效。
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class CorsConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private CorsConfiguration corsConfiguration;

    /**
     * 1. 测试跨域预检请求 (OPTIONS) 是否返回预期的 CORS 响应头
     */
    @Test
    void testCorsConfigurationOptions() throws Exception {
        // 对 /authenticate 发起跨域预检请求
        mockMvc.perform(options("/authenticate")
                .header("Origin", "http://example.com")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type"))
                // 预期返回 200 OK
                .andExpect(status().isOk())
                // 验证是否包含正确的 CORS 响应头
                .andExpect(header().string("Access-Control-Allow-Origin", "http://example.com"))
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().string("Access-Control-Allow-Headers", "Content-Type"));
    }

   
}
