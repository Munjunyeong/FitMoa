package com.asset.flow.goal;

import java.time.LocalDate;

public class Goal {
    private Long goalId;
    private String id;
    private String goalTitle;
    private long targetAmount;
    private long currentAmount;
    private LocalDate endDate;

    public Long getGoalId() { return goalId; }
    public void setGoalId(Long goalId) { this.goalId = goalId; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getGoalTitle() { return goalTitle; }
    public void setGoalTitle(String goalTitle) { this.goalTitle = goalTitle; }
    public long getTargetAmount() { return targetAmount; }
    public void setTargetAmount(long targetAmount) { this.targetAmount = targetAmount; }
    public long getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(long currentAmount) { this.currentAmount = currentAmount; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getProgressPercentage() {
        if (targetAmount <= 0) return 0;
        return (int) Math.min(100, Math.round(currentAmount * 100.0 / targetAmount));
    }
    public long getDaysRemaining() {
        return endDate == null ? 0 : java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), endDate);
    }
}
