package com.ibizabroker.lms.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity @EntityListeners(AuditingEntityListener.class)
@Table(name = "Borrow")
public class Borrow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer borrowId;
    Integer bookId;
    Integer userId;


    // Change the type to Books to establish a relationship
    @ManyToOne(fetch = FetchType.EAGER)  // Lazy loading is preferred for performance reasons
    @JoinColumn(name = "bookId", referencedColumnName = "bookId", insertable = false, updatable = false)
    @JsonInclude(JsonInclude.Include.NON_NULL) // Only include if not null
    private Books book;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonSerialize(using=JsonDataSerializer.class)
    Date issueDate;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonSerialize(using=JsonDataSerializer.class)
    Date returnDate;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonSerialize(using=JsonDataSerializer.class)
    Date dueDate;

}
