package com.ibizabroker.lms;

import com.ibizabroker.lms.dao.UsersRepository;
import com.ibizabroker.lms.dao.BooksRepository;
import com.ibizabroker.lms.entity.Books;
import com.ibizabroker.lms.entity.JwtRequest;
import com.ibizabroker.lms.entity.JwtResponse;
import com.ibizabroker.lms.entity.Role;
import com.ibizabroker.lms.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EndToEndTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private BooksRepository booksRepository;

    @BeforeEach
    public void setUp() {
        // 清空用户和图书数据，保证每次测试环境都是干净的
        usersRepository.deleteAll();
        booksRepository.deleteAll();
    }

    @Test
    public void testUnauthorizedAccess() {
        // 未携带JWT访问受保护的管理员接口，应该返回401 Unauthorized
        ResponseEntity<String> response = restTemplate.getForEntity("/admin/users", String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testEndToEndFlow() {
        // 1. 注册管理员用户（调用 /user/adduser 接口）
        Users adminUser = new Users();
        adminUser.setUsername("admin");
        adminUser.setName("admin");
        adminUser.setPassword("password");
        // 设置角色，注意：这里采用级联保存Role，所以直接new一个Role即可
        Role adminRole = new Role();
        adminRole.setRoleName("Admin");
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        adminUser.setRole(roles);

        ResponseEntity<Users> registerResponse = restTemplate.postForEntity("/user/adduser", adminUser, Users.class);
        assertEquals(HttpStatus.OK, registerResponse.getStatusCode());
        Users registeredAdmin = registerResponse.getBody();
        assertNotNull(registeredAdmin);
        assertNotNull(registeredAdmin.getUserId());

        // 2. 使用 /authenticate 接口认证，获取JWT令牌
        JwtRequest jwtRequest = new JwtRequest();
        jwtRequest.setUserName("admin");
        jwtRequest.setUserPassword("password");

        ResponseEntity<JwtResponse> authResponse = restTemplate.postForEntity("/authenticate", jwtRequest, JwtResponse.class);
        assertEquals(HttpStatus.OK, authResponse.getStatusCode());
        JwtResponse jwtResponse = authResponse.getBody();
        assertNotNull(jwtResponse);
        String token = jwtResponse.getJwtToken();
        assertNotNull(token);

        // 准备包含 JWT 的请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<?> authHeaderEntity = new HttpEntity<>(headers);

        // 3. 访问受保护的管理员用户列表接口（/admin/users）
        ResponseEntity<Users[]> adminUsersResponse = restTemplate.exchange("/admin/users", HttpMethod.GET, authHeaderEntity, Users[].class);
        assertEquals(HttpStatus.OK, adminUsersResponse.getStatusCode());
        Users[] usersArray = adminUsersResponse.getBody();
        assertNotNull(usersArray);
        assertTrue(usersArray.length > 0);

        // 4. 图书管理测试：创建一本图书（POST /admin/books）
        Books newBook = new Books();
        newBook.setBookName("测试图书");
        newBook.setBookAuthor("测试作者");
        newBook.setBookGenre("小说");
        newBook.setNoOfCopies(5);

        ResponseEntity<Books> createBookResponse = restTemplate.exchange(
                "/admin/books",
                HttpMethod.POST,
                new HttpEntity<>(newBook, headers),
                Books.class
        );
        assertEquals(HttpStatus.OK, createBookResponse.getStatusCode());
        Books createdBook = createBookResponse.getBody();
        assertNotNull(createdBook);
        assertNotNull(createdBook.getBookId());

        // 5. 根据ID获取图书（GET /admin/books/{id}）
        ResponseEntity<Books> getBookResponse = restTemplate.exchange(
                "/admin/books/" + createdBook.getBookId(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Books.class
        );
        assertEquals(HttpStatus.OK, getBookResponse.getStatusCode());
        Books retrievedBook = getBookResponse.getBody();
        assertNotNull(retrievedBook);
        assertEquals("测试图书", retrievedBook.getBookName());

        // 6. 更新图书（PUT /admin/books/{id}）
        retrievedBook.setNoOfCopies(10);
        HttpEntity<Books> updateEntity = new HttpEntity<>(retrievedBook, headers);
        ResponseEntity<Books> updateResponse = restTemplate.exchange(
                "/admin/books/" + retrievedBook.getBookId(),
                HttpMethod.PUT,
                updateEntity,
                Books.class
        );
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        Books updatedBook = updateResponse.getBody();
        assertNotNull(updatedBook);
        assertEquals(10, updatedBook.getNoOfCopies());

        // 7. 删除图书（DELETE /admin/books/{id}）
        ResponseEntity<?> deleteResponse = restTemplate.exchange(
                "/admin/books/" + createdBook.getBookId(),
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Object.class
        );
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());

        // 8. 验证图书已删除（GET /admin/books/{id} 应返回 404 Not Found）
        ResponseEntity<Books> getDeletedBookResponse = restTemplate.exchange(
                "/admin/books/" + createdBook.getBookId(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Books.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getDeletedBookResponse.getStatusCode());
    }
}
