package com.example.kakeibo.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.kakeibo.entity.Transaction;
import com.example.kakeibo.repository.TransactionRepository;

@Controller
public class KakeiboController {

    private final TransactionRepository transactionRepository;

    public KakeiboController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/")
    public String index(
            @RequestParam(required = false) String month,
            Model model) {

        List<Transaction> transactions;

        if (month != null && !month.isEmpty()) {

            YearMonth yearMonth = YearMonth.parse(month);

            LocalDate start = yearMonth.atDay(1);
            LocalDate end = yearMonth.atEndOfMonth();

            transactions = transactionRepository.findByDateBetween(start, end);

        } else {

            transactions = transactionRepository.findAll();
        }

        int incomeTotal = transactions.stream()
                .filter(t -> t.getType() != null)
                .filter(t -> t.getType().trim().equals("収入"))
                .filter(t -> t.getAmount() != null)
                .mapToInt(Transaction::getAmount)
                .sum();

        int expenseTotal = transactions.stream()
                .filter(t -> t.getType() != null)
                .filter(t -> t.getType().trim().equals("支出"))
                .filter(t -> t.getAmount() != null)
                .mapToInt(Transaction::getAmount)
                .sum();

        int balance = incomeTotal - expenseTotal;
        Map<String, Integer> categoryTotals = transactions.stream()
                .filter(t -> "支出".equals(t.getType()))
                .filter(t -> t.getCategory() != null)
                .filter(t -> t.getAmount() != null)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingInt(Transaction::getAmount)
                ));

        model.addAttribute("transactions", transactions);
        model.addAttribute("incomeTotal", incomeTotal);
        model.addAttribute("expenseTotal", expenseTotal);
        model.addAttribute("balance", balance);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("categoryLabels", categoryTotals.keySet());
        model.addAttribute("categoryValues", categoryTotals.values());

        return "index";
    }
    @GetMapping("/new")
    public String newTransaction(Model model) {
        model.addAttribute("transaction", new Transaction());
        return "form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Transaction transaction) {
        transactionRepository.save(transaction);
        return "redirect:/";
    }
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Transaction transaction = transactionRepository.findById(id).orElseThrow();
        model.addAttribute("transaction", transaction);
        return "form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        transactionRepository.deleteById(id);
        return "redirect:/";
    }
}