package com.example.kakeibo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kakeibo.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByDateBetween(LocalDate start, LocalDate end);

}