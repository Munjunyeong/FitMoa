package com.asset.flow.goal;

import java.util.List;

public interface GoalService {
    List<Goal> getGoals(String ownerId);
    Goal getGoal(Long goalId, String ownerId);
    void createGoal(String ownerId, Goal goal);
    void updateGoal(Long goalId, String ownerId, Goal goal);
    void deposit(Long goalId, String ownerId, String bankAccountId, long amount);
    void withdraw(Long goalId, String ownerId, String bankAccountId, long amount);
    void deleteGoal(Long goalId, String ownerId);
}
