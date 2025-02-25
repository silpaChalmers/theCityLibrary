package com.ibizabroker.lms.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BooksTest {

    @Test
    void testBooksEntity() {
        Books book = new Books();
        book.setBookId(1);
        book.setBookName("Test Book");
        book.setBookAuthor("Test Author");
        book.setBookGenre("Fiction");
        book.setNoOfCopies(5);

        assertEquals(1, book.getBookId());
        assertEquals("Test Book", book.getBookName());
        assertEquals("Test Author", book.getBookAuthor());
        assertEquals("Fiction", book.getBookGenre());
        assertEquals(5, book.getNoOfCopies());
    }

    @Test
    void testBorrowBook() {
        Books book = new Books();
        book.setNoOfCopies(3);
        book.borrowBook();
        assertEquals(2, book.getNoOfCopies());
    }

    @Test
    void testBorrowBookWithZeroCopies() {
        Books book = new Books();
        book.setNoOfCopies(0);

        Exception exception = assertThrows(IllegalStateException.class, book::borrowBook);
        assertEquals("No copies left to borrow.", exception.getMessage());
    }

    @Test
    void testBorrowBookMutationSurvival() {
        Books book = new Books();
        book.setNoOfCopies(3);
        book.borrowBook();
        book.borrowBook();
        assertEquals(1, book.getNoOfCopies());
    }

    @Test
    void testBorrowBookAtBoundary() {
        Books book = new Books();
        book.setNoOfCopies(1); // 仅剩 1 本书

        book.borrowBook(); // 应该成功借出
        assertEquals(0, book.getNoOfCopies(), "Copy number should be 0");
    }


    @Test
    void testReturnBook() {
        Books book = new Books();
        book.setNoOfCopies(3);
        book.returnBook();
        assertEquals(4, book.getNoOfCopies());
    }

    @Test
    void testReturnBookMultipleTimes() {
        Books book = new Books();
        book.setNoOfCopies(1);

        book.returnBook();
        assertEquals(2, book.getNoOfCopies());

        book.returnBook();
        assertEquals(3, book.getNoOfCopies());
    }

    @Test
    void testReturnBookAtBoundary() {
        Books book = new Books();
        book.setNoOfCopies(0); // 库存为 0

        book.returnBook(); // 归还一本
        assertEquals(1, book.getNoOfCopies(), "Copy number should be added");
    }


    @Test
    void testEqualsAndHashCode() {
        Books book1 = new Books();
        book1.setBookId(1);
        book1.setBookName("Test Book");

        Books book2 = new Books();
        book2.setBookId(1);
        book2.setBookName("Test Book");

        Books book3 = new Books();
        book3.setBookId(2);
        book3.setBookName("Another Book");

        assertEquals(book1, book2);
        assertNotEquals(book1, book3);
        assertEquals(book1.hashCode(), book2.hashCode());
        assertNotEquals(book1.hashCode(), book3.hashCode());
    }

    @Test
    void testToString() {
        Books book = new Books();
        book.setBookId(1);
        book.setBookName("Test Book");
        String bookString = book.toString();
        assertTrue(bookString.contains("Test Book"));
        assertTrue(bookString.contains("1"));
    }

    @Test
    void testCanEqual() {
        Books book1 = new Books();
        Books book2 = new Books();
        assertTrue(book1.canEqual(book2));
        assertFalse(book1.canEqual(new Object()));
    }

    @Test
    void testMutatedIntegerOperations() {
        Books book = new Books();
        book.setNoOfCopies(5);

        book.borrowBook();
        book.returnBook();
        assertEquals(5, book.getNoOfCopies());
    }
}
