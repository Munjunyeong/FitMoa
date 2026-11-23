package com.asset.flow.expense;

import java.time.LocalDate;

public class Expense {
    private Long expenseId;
    private String id;
    private String categoryName;
    private Long amount;
    private LocalDate spentAt;

    public Long getExpenseId() { return expenseId; }
    public void setExpenseId(Long expenseId) { this.expenseId = expenseId; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    public LocalDate getSpentAt() { return spentAt; }
    public void setSpentAt(LocalDate spentAt) { this.spentAt = spentAt; }
}
