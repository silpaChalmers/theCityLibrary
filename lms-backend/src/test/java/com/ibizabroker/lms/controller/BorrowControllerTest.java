package com.ibizabroker.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibizabroker.lms.dao.BooksRepository;
import com.ibizabroker.lms.dao.BorrowRepository;
import com.ibizabroker.lms.dao.UsersRepository;
import com.ibizabroker.lms.entity.Books;
import com.ibizabroker.lms.entity.Borrow;
import com.ibizabroker.lms.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 测试 BorrowController 的主要方法
 */
@WebMvcTest(BorrowController.class)
// 如果 BorrowController 上还有 @Repository，会导致一些扫描冲突，可在此加上 ComponentScan 或移除 @Repository
@ComponentScan(basePackages = "com.ibizabroker.lms")
class BorrowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BorrowRepository borrowRepository;

    @MockBean
    private UsersRepository usersRepository;

    @MockBean
    private BooksRepository booksRepository;

    @Autowired
    private ObjectMapper objectMapper; // 用于序列化/反序列化 JSON

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试借书接口 POST /borrow 成功场景
     */
    @Test
    void testBorrowBook_Success() throws Exception {
        // 构造请求体
        Borrow borrowRequest = new Borrow();
        borrowRequest.setUserId(1);
        borrowRequest.setBookId(2);

        // 模拟数据库返回的 User、Book
        Users mockUser = new Users();
        mockUser.setUserId(1);
        mockUser.setName("Test User");

        Books mockBook = new Books();
        mockBook.setBookId(2);
        mockBook.setBookName("Test Book");
        mockBook.setNoOfCopies(3); // 初始库存 3

        // Mock 行为：usersRepository、booksRepository 查到对应的对象
        when(usersRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(booksRepository.findById(2)).thenReturn(Optional.of(mockBook));

        // 使用 ArgumentCaptor 捕获调用参数
        ArgumentCaptor<Books> booksCaptor = ArgumentCaptor.forClass(Books.class);
        ArgumentCaptor<Borrow> borrowCaptor = ArgumentCaptor.forClass(Borrow.class);

        // 记录调用借书前的系统时间
        long beforeCall = System.currentTimeMillis();

        // Mock 行为：borrowRepository.save(...) 会给 borrow 设置一个主键
        when(borrowRepository.save(any(Borrow.class))).thenAnswer(invocation -> {
            Borrow b = invocation.getArgument(0);
            b.setBorrowId(100);
            return b;
        });

        // 发送 POST /borrow
        mockMvc.perform(post("/borrow")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Test User has borrowed one copy of \"Test Book\"!"));

        // 验证 Books 库存减少 1
        verify(booksRepository, times(1)).save(booksCaptor.capture());
        Books savedBook = booksCaptor.getValue();
        assertEquals(2, savedBook.getNoOfCopies(), "库存应由 3 减为 2，表示 borrowBook() 被调用");

        // 验证 Borrow 被保存，并检查日期字段被设置
        verify(borrowRepository, times(1)).save(borrowCaptor.capture());
        Borrow savedBorrow = borrowCaptor.getValue();
        assertNotNull(savedBorrow.getIssueDate(), "issueDate 应被设置");
        assertNotNull(savedBorrow.getDueDate(), "dueDate 应被设置");

        // 验证 dueDate 等于 issueDate 加 7 天（604800000 毫秒）
        long diff = savedBorrow.getDueDate().getTime() - savedBorrow.getIssueDate().getTime();
        long sevenDaysInMs = 7L * 24 * 60 * 60 * 1000;
        assertEquals(sevenDaysInMs, diff, "dueDate 应等于 issueDate 加 7 天");

        // 新增：验证 issueDate 与借书请求时的系统时间接近（例如差值小于 1 秒）
        long diffIssue = savedBorrow.getIssueDate().getTime() - beforeCall;
        assertTrue(diffIssue >= 0 && diffIssue < 1000, "issueDate 应接近调用时间");
    }

    /**
     * 新增测试：库存正好为 1 的边界条件
     * 期望：借书成功，库存从 1 变为 0
     */
    @Test
    void testBorrowBook_BoundaryCondition() throws Exception {
        Borrow borrowRequest = new Borrow();
        borrowRequest.setUserId(1);
        borrowRequest.setBookId(2);

        Users mockUser = new Users();
        mockUser.setUserId(1);
        mockUser.setName("Test User");

        Books mockBook = new Books();
        mockBook.setBookId(2);
        mockBook.setBookName("Boundary Book");
        mockBook.setNoOfCopies(1); // 边界条件库存为 1

        when(usersRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(booksRepository.findById(2)).thenReturn(Optional.of(mockBook));

        // 使用 ArgumentCaptor 捕获调用参数
        ArgumentCaptor<Books> booksCaptor = ArgumentCaptor.forClass(Books.class);
        ArgumentCaptor<Borrow> borrowCaptor = ArgumentCaptor.forClass(Borrow.class);

        when(borrowRepository.save(any(Borrow.class))).thenAnswer(invocation -> {
            Borrow b = invocation.getArgument(0);
            b.setBorrowId(200);
            return b;
        });

        mockMvc.perform(post("/borrow")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Test User has borrowed one copy of \"Boundary Book\"!"));

        verify(booksRepository, times(1)).save(booksCaptor.capture());
        Books savedBook = booksCaptor.getValue();
        assertEquals(0, savedBook.getNoOfCopies(), "库存应由 1 减为 0，验证边界条件");

        verify(borrowRepository, times(1)).save(borrowCaptor.capture());
        Borrow savedBorrow = borrowCaptor.getValue();
        assertNotNull(savedBorrow.getIssueDate(), "issueDate 应被设置");
        assertNotNull(savedBorrow.getDueDate(), "dueDate 应被设置");

        long diff = savedBorrow.getDueDate().getTime() - savedBorrow.getIssueDate().getTime();
        long sevenDaysInMs = 7L * 24 * 60 * 60 * 1000;
        assertEquals(sevenDaysInMs, diff, "dueDate 应等于 issueDate 加 7 天");
    }

    /**
     * 测试借书接口 POST /borrow 当库存不足时
     */
    @Test
    void testBorrowBook_OutOfStock() throws Exception {
        Borrow borrowRequest = new Borrow();
        borrowRequest.setUserId(1);
        borrowRequest.setBookId(2);

        Users mockUser = new Users();
        mockUser.setUserId(1);
        mockUser.setName("Test User");

        Books mockBook = new Books();
        mockBook.setBookId(2);
        mockBook.setBookName("Test Book");
        mockBook.setNoOfCopies(0); // 库存为 0

        when(usersRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(booksRepository.findById(2)).thenReturn(Optional.of(mockBook));

        // 发起借书请求
        mockMvc.perform(post("/borrow")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("The book \"Test Book\" is out of stock!"));

        // 确保 borrowRepository 未保存任何记录
        verify(borrowRepository, never()).save(any(Borrow.class));
    }

    /**
     * 测试 GET /borrow - 获取所有借阅记录
     */
    @Test
    void testGetAllBorrow() throws Exception {
        Borrow b1 = new Borrow();
        b1.setBorrowId(101);
        b1.setUserId(1);
        b1.setBookId(10);

        Borrow b2 = new Borrow();
        b2.setBorrowId(102);
        b2.setUserId(2);
        b2.setBookId(20);

        List<Borrow> mockList = Arrays.asList(b1, b2);

        when(borrowRepository.findAll()).thenReturn(mockList);

        mockMvc.perform(get("/borrow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].borrowId").value(101))
                .andExpect(jsonPath("$[1].borrowId").value(102));
    }

    /**
     * 测试 PUT /borrow - 归还图书
     */
    @Test
    void testReturnBook_Success() throws Exception {
        // 已存在的借阅记录
        Borrow existingBorrow = new Borrow();
        existingBorrow.setBorrowId(201);
        existingBorrow.setUserId(1);
        existingBorrow.setBookId(2);

        Books mockBook = new Books();
        mockBook.setBookId(2);
        mockBook.setNoOfCopies(3);

        when(borrowRepository.findById(201)).thenReturn(Optional.of(existingBorrow));
        when(booksRepository.findById(2)).thenReturn(Optional.of(mockBook));

        when(borrowRepository.save(any(Borrow.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 前端发起归还请求
        Borrow requestBody = new Borrow();
        requestBody.setBorrowId(201);

        mockMvc.perform(put("/borrow")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.borrowId").value(201))
                .andExpect(jsonPath("$.returnDate").isNotEmpty());

        // 验证库存加 1
        ArgumentCaptor<Books> bookCaptor = ArgumentCaptor.forClass(Books.class);
        verify(booksRepository).save(bookCaptor.capture());
        assertEquals(4, bookCaptor.getValue().getNoOfCopies());
    }

    /**
     * 测试 GET /borrow/user/{id} - 根据用户ID查询借阅记录
     */
    @Test
    void testBooksBorrowedByUser() throws Exception {
        Borrow b1 = new Borrow();
        b1.setBorrowId(301);
        b1.setUserId(1);
        b1.setBookId(10);

        Borrow b2 = new Borrow();
        b2.setBorrowId(302);
        b2.setUserId(1);
        b2.setBookId(20);

        when(borrowRepository.findByUserId(1)).thenReturn(Arrays.asList(b1, b2));

        mockMvc.perform(get("/borrow/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].borrowId").value(301))
                .andExpect(jsonPath("$[1].borrowId").value(302));
    }

    /**
     * 测试 GET /borrow/book/{id} - 根据图书ID查询借阅记录
     */
    @Test
    void testBookBorrowHistory() throws Exception {
        Borrow b1 = new Borrow();
        b1.setBorrowId(401);
        b1.setUserId(1);
        b1.setBookId(2);

        when(borrowRepository.findByBookId(2)).thenReturn(Collections.singletonList(b1));

        mockMvc.perform(get("/borrow/book/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].borrowId").value(401));
    }
}
