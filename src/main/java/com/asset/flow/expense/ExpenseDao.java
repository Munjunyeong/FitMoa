package com.asset.flow.expense;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseDao {
    List<Expense> findByOwnerAndPeriod(String ownerId, LocalDate fromDate, LocalDate untilDate);
    Expense findOwned(Long expenseId, String ownerId);
    List<ExpenseCategorySummary> summarizeByCategory(String ownerId, LocalDate fromDate, LocalDate untilDate);
    int insert(Expense expense);
    int updateOwned(Expense expense);
    int deleteOwned(Long expenseId, String ownerId);
}
