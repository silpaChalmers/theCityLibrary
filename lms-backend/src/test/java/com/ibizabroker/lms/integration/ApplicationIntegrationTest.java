package com.ibizabroker.lms.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibizabroker.lms.dao.BooksRepository;
import com.ibizabroker.lms.dao.UsersRepository;
import com.ibizabroker.lms.entity.Books;
import com.ibizabroker.lms.entity.JwtResponse;
import com.ibizabroker.lms.entity.Role;
import com.ibizabroker.lms.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private BooksRepository booksRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ObjectMapper objectMapper = new ObjectMapper();

    private String adminToken;
    

    @BeforeEach
    public void setup() throws Exception {
        // 清空用户和图书数据，确保测试隔离
        usersRepository.deleteAll();
        booksRepository.deleteAll();

        // 创建一个拥有 Admin 角色的管理员用户
        Role adminRole = new Role();
        adminRole.setRoleName("Admin");

        Users admin = new Users();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setName("Administrator");
        admin.setRole(Set.of(adminRole));
        usersRepository.save(admin);

        // 通过 /authenticate 接口获取 JWT token
        String authRequestJson = "{\"username\": \"admin\", \"password\": \"admin123\"}";
        MvcResult authResult = mockMvc.perform(post("/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtToken").exists())
                .andReturn();

        JwtResponse jwtResponse = objectMapper.readValue(authResult.getResponse().getContentAsString(), JwtResponse.class);
        adminToken = jwtResponse.getJwtToken();
    }

    @Test
    public void testAuthenticationSuccess() throws Exception {
        // 测试正确的认证请求返回 JWT token
        String authRequestJson = "{\"username\": \"admin\", \"password\": \"admin123\"}";
        mockMvc.perform(post("/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtToken").exists());
    }

    @Test
    public void testAuthenticationFailure() throws Exception {
        // 测试错误密码时返回 4xx（认证失败）
        String authRequestJson = "{\"username\": \"admin\", \"password\": \"wrongpassword\"}";
        mockMvc.perform(post("/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authRequestJson))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testUserRegistration() throws Exception {
        // 测试通过 /user/adduser 注册新用户
        String newUserJson = "{\"username\": \"user1\", \"name\": \"User One\", \"password\": \"password1\"}";
        MvcResult result = mockMvc.perform(post("/user/adduser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newUserJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"))
                .andReturn();

        Users createdUser = objectMapper.readValue(result.getResponse().getContentAsString(), Users.class);
        // 验证存储的密码已被加密，不等于明文
        assertNotEquals("password1", createdUser.getPassword());
    }

    @Test
    public void testAdminBooksCRUD() throws Exception {
        // 1. 通过受保护的 POST /admin/books 添加一本图书
        String bookJson = "{\"bookName\": \"Integration Test Book\", \"bookAuthor\": \"Test Author\", \"bookGenre\": \"Test Genre\", \"noOfCopies\": 5}";
        MvcResult createResult = mockMvc.perform(post("/admin/books")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookName").value("Integration Test Book"))
                .andReturn();

        Books createdBook = objectMapper.readValue(createResult.getResponse().getContentAsString(), Books.class);
        Integer bookId = createdBook.getBookId();

        // 2. 使用 GET /admin/books/{id} 获取图书详情
        mockMvc.perform(get("/admin/books/" + bookId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookName").value("Integration Test Book"));

        // 3. 使用 PUT /admin/books/{id} 更新图书信息
        String updatedBookJson = "{\"bookName\": \"Updated Book Name\", \"bookAuthor\": \"Updated Author\", \"bookGenre\": \"Updated Genre\", \"noOfCopies\": 10}";
        mockMvc.perform(put("/admin/books/" + bookId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedBookJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookName").value("Updated Book Name"));

        // 4. 使用 DELETE /admin/books/{id} 删除图书
        mockMvc.perform(delete("/admin/books/" + bookId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(true));
    }

    @Test
    public void testAdminUsersManagement() throws Exception {
        // 通过 /admin/users 管理接口测试管理员对用户的增查改操作

        // 1. 管理员通过 POST /admin/users 添加新用户
        String newUserJson = "{\"username\": \"user2\", \"name\": \"User Two\", \"password\": \"password2\"}";
        MvcResult result = mockMvc.perform(post("/admin/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(newUserJson))
                .andExpect(status().isOk())
                .andReturn();

        Users createdUser = objectMapper.readValue(result.getResponse().getContentAsString(), Users.class);
        Integer userId = createdUser.getUserId();

        // 2. 管理员使用 GET /admin/users 获取所有用户
        mockMvc.perform(get("/admin/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 3. 管理员使用 GET /admin/users/{id} 获取指定用户详情
        mockMvc.perform(get("/admin/users/" + userId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user2"));

        // 4. 管理员使用 PUT /admin/users/{id} 更新用户信息
        String updatedUserJson = "{\"username\": \"user2_updated\", \"name\": \"User Two Updated\"}";
        mockMvc.perform(put("/admin/users/" + userId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedUserJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user2_updated"));
    }

    @Test
    public void testPublicAccessBooksList() throws Exception {
        // 测试公开访问的 GET /admin/books/ 接口（安全配置中允许 /admin/books/ 访问）
        // 先添加一本图书
        String bookJson = "{\"bookName\": \"Public Book\", \"bookAuthor\": \"Author\", \"bookGenre\": \"Genre\", \"noOfCopies\": 3}";
        mockMvc.perform(post("/admin/books")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookJson))
                .andExpect(status().isOk());

        // 然后以不带 token 的方式访问 GET /admin/books/（注意尾部斜杠，确保匹配 permitAll 的配置）
        mockMvc.perform(get("/admin/books/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testBorrowEndpointNotFound() throws Exception {
        // 由于 BorrowController 中的业务方法尚未实现，访问 /borrow 时应返回 404
        mockMvc.perform(get("/borrow"))
                .andExpect(status().isNotFound());
    }
}
