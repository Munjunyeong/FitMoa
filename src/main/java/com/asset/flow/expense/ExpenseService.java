package com.asset.flow.expense;

import java.time.YearMonth;
import java.util.List;

public interface ExpenseService {
    List<Expense> getMonthlyExpenses(String ownerId, YearMonth month);
    List<ExpenseCategorySummary> getCategorySummary(String ownerId, YearMonth month);
    Expense getExpense(Long expenseId, String ownerId);
    void createExpense(String ownerId, Expense expense);
    void updateExpense(Long expenseId, String ownerId, Expense expense);
    void deleteExpense(Long expenseId, String ownerId);
}
