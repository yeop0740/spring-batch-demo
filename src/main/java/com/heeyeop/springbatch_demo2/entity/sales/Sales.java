package com.heeyeop.springbatch_demo2.entity.sales;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Entity
public class Sales {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate orderDate;
    private long amount;
    private String orderNo;

    @Builder
    public Sales(LocalDate orderDate, long amount, String orderNo) {
        this.orderDate = orderDate;
        this.amount = amount;
        this.orderNo = orderNo;
    }
}
