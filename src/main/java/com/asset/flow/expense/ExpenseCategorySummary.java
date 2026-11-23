package com.asset.flow.expense;

public class ExpenseCategorySummary {
    private String categoryName;
    private Long totalAmount;
    private Long expenseCount;
    private int percentage;

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Long getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Long totalAmount) { this.totalAmount = totalAmount; }
    public Long getExpenseCount() { return expenseCount; }
    public void setExpenseCount(Long expenseCount) { this.expenseCount = expenseCount; }
    public int getPercentage() { return percentage; }
    public void setPercentage(int percentage) { this.percentage = percentage; }
}
