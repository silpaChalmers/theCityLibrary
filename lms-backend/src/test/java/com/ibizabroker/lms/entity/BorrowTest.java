package com.ibizabroker.lms.entity;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class BorrowTest {

    @Test
    void testBorrowEntity() {
        Borrow borrow = new Borrow();
        borrow.setBorrowId(1);
        borrow.setBookId(101);
        borrow.setUserId(202);
        Date issueDate = new Date();
        Date returnDate = new Date();
        Date dueDate = new Date();

        borrow.setIssueDate(issueDate);
        borrow.setReturnDate(returnDate);
        borrow.setDueDate(dueDate);

        assertEquals(1, borrow.getBorrowId());
        assertEquals(101, borrow.getBookId());
        assertEquals(202, borrow.getUserId());
        assertEquals(issueDate, borrow.getIssueDate());
        assertEquals(returnDate, borrow.getReturnDate());
        assertEquals(dueDate, borrow.getDueDate());
    }

    @Test
    void testEqualsAndHashCode() {
        Borrow borrow1 = new Borrow();
        borrow1.setBorrowId(1);
        borrow1.setBookId(101);
        borrow1.setUserId(202);

        Borrow borrow2 = new Borrow();
        borrow2.setBorrowId(1);
        borrow2.setBookId(101);
        borrow2.setUserId(202);

        Borrow borrow3 = new Borrow();
        borrow3.setBorrowId(2);
        borrow3.setBookId(102);
        borrow3.setUserId(203);

        assertEquals(borrow1, borrow2);
        assertNotEquals(borrow1, borrow3);
        assertEquals(borrow1.hashCode(), borrow2.hashCode());
        assertNotEquals(borrow1.hashCode(), borrow3.hashCode());
    }

    @Test
    void testToString() {
        Borrow borrow = new Borrow();
        borrow.setBorrowId(1);
        borrow.setBookId(101);
        borrow.setUserId(202);

        String borrowString = borrow.toString();
        assertTrue(borrowString.contains("1"));
        assertTrue(borrowString.contains("101"));
        assertTrue(borrowString.contains("202"));
    }

    @Test
    void testCanEqual() {
        Borrow borrow1 = new Borrow();
        Borrow borrow2 = new Borrow();
        assertTrue(borrow1.canEqual(borrow2));
        assertFalse(borrow1.canEqual(new Object()));
    }

    @Test
    void testNullValues() {
        Borrow borrow = new Borrow();
        borrow.setIssueDate(null);
        borrow.setReturnDate(null);
        borrow.setDueDate(null);

        assertNull(borrow.getIssueDate());
        assertNull(borrow.getReturnDate());
        assertNull(borrow.getDueDate());
    }

    @Test
    void testMutatedIntegerOperations() {
        Borrow borrow = new Borrow();
        borrow.setBorrowId(5);
        borrow.setBookId(10);
        borrow.setUserId(15);

        assertEquals(5, borrow.getBorrowId());
        assertEquals(10, borrow.getBookId());
        assertEquals(15, borrow.getUserId());
    }
}
