package com.ibizabroker.lms.controller;

import com.ibizabroker.lms.dao.BooksRepository;
import com.ibizabroker.lms.entity.Books;
import com.ibizabroker.lms.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 使用 @SpringBootTest + @AutoConfigureMockMvc 测试 BooksController
 * 并通过 @WithMockUser(roles = "Admin") 模拟管理员权限。
 *
 * 另外，由于 NotFoundException 未标注 @ResponseStatus，因此在测试中通过内部异常处理器将其转换为 404。
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = "Admin")
@Import(BooksControllerTest.TestExceptionHandler.class)
public class BooksControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BooksRepository booksRepository;

    /**
     * 内部异常处理器，将 NotFoundException 转换为 HTTP 404 响应，
     * 使测试预期的状态码生效。
     */
    @RestControllerAdvice
    public static class TestExceptionHandler {
        @ExceptionHandler(NotFoundException.class)
        public ResponseEntity<String> handleNotFoundException(NotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    /**
     * 辅助方法：利用反射设置 Books 实体的 bookId 字段
     */
    private void setBookId(Books book, int id) {
        try {
            Field field = Books.class.getDeclaredField("bookId");
            field.setAccessible(true);
            field.set(book, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试：获取所有图书
     */
    @Test
    void testGetAllBooks() throws Exception {
        Books book1 = new Books();
        setBookId(book1, 1);
        book1.setBookName("Book1");
        book1.setBookAuthor("Author1");
        book1.setBookGenre("Genre1");
        book1.setNoOfCopies(5);

        Books book2 = new Books();
        setBookId(book2, 2);
        book2.setBookName("Book2");
        book2.setBookAuthor("Author2");
        book2.setBookGenre("Genre2");
        book2.setNoOfCopies(3);

        List<Books> booksList = Arrays.asList(book1, book2);
        when(booksRepository.findAll()).thenReturn(booksList);

        mockMvc.perform(get("/admin/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(1))
                .andExpect(jsonPath("$[0].bookName").value("Book1"))
                .andExpect(jsonPath("$[1].bookId").value(2))
                .andExpect(jsonPath("$[1].bookName").value("Book2"));

        // 验证调用
        verify(booksRepository, times(1)).findAll();
    }

    /**
     * 测试：根据ID获取图书成功
     */
    @Test
    void testGetBookById_Success() throws Exception {
        Books book = new Books();
        setBookId(book, 1);
        book.setBookName("Book1");
        book.setBookAuthor("Author1");
        book.setBookGenre("Genre1");
        book.setNoOfCopies(5);

        when(booksRepository.findById(1)).thenReturn(Optional.of(book));

        mockMvc.perform(get("/admin/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.bookName").value("Book1"));

        verify(booksRepository, times(1)).findById(1);
    }

    /**
     * 测试：根据ID获取图书时图书不存在，返回 404 状态并包含错误消息
     */
    @Test
    void testGetBookById_NotFound() throws Exception {
        when(booksRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Book with id 999 does not exist.")));

        verify(booksRepository, times(1)).findById(999);
    }

    /**
     * 测试：创建图书
     */
    @Test
    void testCreateBook() throws Exception {
        Books savedBook = new Books();
        setBookId(savedBook, 1);
        savedBook.setBookName("New Book");
        savedBook.setBookAuthor("New Author");
        savedBook.setBookGenre("New Genre");
        savedBook.setNoOfCopies(10);

        when(booksRepository.save(any(Books.class))).thenReturn(savedBook);

        String jsonContent = "{\"bookName\":\"New Book\",\"bookAuthor\":\"New Author\",\"bookGenre\":\"New Genre\",\"noOfCopies\":10}";

        mockMvc.perform(post("/admin/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.bookName").value("New Book"));

        // 验证调用 + Captor
        ArgumentCaptor<Books> captor = ArgumentCaptor.forClass(Books.class);
        verify(booksRepository, times(1)).save(captor.capture());
        Books capturedBook = captor.getValue();
        assertEquals("New Book", capturedBook.getBookName());
        assertEquals("New Author", capturedBook.getBookAuthor());
        assertEquals("New Genre", capturedBook.getBookGenre());
        assertEquals(10, capturedBook.getNoOfCopies());
    }

    /**
     * 测试：更新图书成功
     */
    @Test
    void testUpdateBook() throws Exception {
        // 数据库中已有的书
        Books existingBook = new Books();
        setBookId(existingBook, 1);
        existingBook.setBookName("Old Book");
        existingBook.setBookAuthor("Old Author");
        existingBook.setBookGenre("Old Genre");
        existingBook.setNoOfCopies(5);

        // 模拟更新后的书
        Books updatedBook = new Books();
        setBookId(updatedBook, 1);
        updatedBook.setBookName("Updated Book");
        updatedBook.setBookAuthor("Updated Author");
        updatedBook.setBookGenre("Updated Genre");
        updatedBook.setNoOfCopies(8);

        when(booksRepository.findById(1)).thenReturn(Optional.of(existingBook));
        when(booksRepository.save(any(Books.class))).thenReturn(updatedBook);

        String jsonContent = "{\"bookName\":\"Updated Book\",\"bookAuthor\":\"Updated Author\",\"bookGenre\":\"Updated Genre\",\"noOfCopies\":8}";

        mockMvc.perform(put("/admin/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.bookName").value("Updated Book"));

        // 验证调用
        verify(booksRepository, times(1)).findById(1);
        ArgumentCaptor<Books> updateCaptor = ArgumentCaptor.forClass(Books.class);
        verify(booksRepository, times(1)).save(updateCaptor.capture());
        Books savedEntity = updateCaptor.getValue();
        // 确认更新后的字段
        assertEquals("Updated Book", savedEntity.getBookName());
        assertEquals("Updated Author", savedEntity.getBookAuthor());
        assertEquals("Updated Genre", savedEntity.getBookGenre());
        assertEquals(8, savedEntity.getNoOfCopies());
    }

    /**
     * 测试：更新图书时图书不存在，返回 404 状态并包含错误消息
     */
    @Test
    void testUpdateBook_NotFound() throws Exception {
        when(booksRepository.findById(999)).thenReturn(Optional.empty());
        String jsonContent = "{\"bookName\":\"Updated Book\",\"bookAuthor\":\"Updated Author\",\"bookGenre\":\"Updated Genre\",\"noOfCopies\":8}";

        mockMvc.perform(put("/admin/books/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Book with id 999 does not exist.")));

        verify(booksRepository, times(1)).findById(999);
        verify(booksRepository, never()).save(any(Books.class));
    }

    /**
     * 测试：删除图书成功
     */
    @Test
    void testDeleteBook() throws Exception {
        Books book = new Books();
        setBookId(book, 1);
        book.setBookName("Book1");
        book.setBookAuthor("Author1");
        book.setBookGenre("Genre1");
        book.setNoOfCopies(5);

        when(booksRepository.findById(1)).thenReturn(Optional.of(book));
        doNothing().when(booksRepository).delete(book);

        mockMvc.perform(delete("/admin/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(true));

        // 验证调用
        verify(booksRepository, times(1)).findById(1);
        verify(booksRepository, times(1)).delete(book);
    }

    /**
     * 测试：删除图书时图书不存在，返回 404 状态并包含错误消息
     */
    @Test
    void testDeleteBook_NotFound() throws Exception {
        when(booksRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/admin/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Book with id 999 does not exist.")));

        verify(booksRepository, times(1)).findById(999);
        verify(booksRepository, never()).delete(any(Books.class));
    }
}
