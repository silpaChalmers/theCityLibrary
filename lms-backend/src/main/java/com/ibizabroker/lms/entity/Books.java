package com.ibizabroker.lms.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "Books")
public class Books {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer bookId;
    String bookName;
    String bookAuthor;
    String bookGenre;
    Integer noOfCopies;

    public void borrowBook() {
        if (this.noOfCopies <= 0) {
            throw new IllegalStateException("No copies left to borrow.");
        }
        this.noOfCopies--;
    }

    public void returnBook() {
        this.noOfCopies++;
    }

}
