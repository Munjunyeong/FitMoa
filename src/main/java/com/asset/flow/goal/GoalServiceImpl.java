package com.asset.flow.goal;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class GoalServiceImpl implements GoalService {
    private final GoalDao goalDao;
    public GoalServiceImpl(GoalDao goalDao) { this.goalDao = goalDao; }

    @Override public List<Goal> getGoals(String ownerId) { return goalDao.findAllByOwner(ownerId); }
    @Override public Goal getGoal(Long goalId, String ownerId) {
        Goal goal = goalDao.findOwned(goalId, ownerId);
        if (goal == null) throw new IllegalArgumentException("저축 목표를 찾을 수 없습니다.");
        return goal;
    }
    @Override public void createGoal(String ownerId, Goal goal) {
        validate(goal);
        goal.setId(ownerId);
        goal.setCurrentAmount(0);
        goalDao.insert(goal);
    }
    @Override public void updateGoal(Long goalId, String ownerId, Goal form) {
        Goal current = getGoal(goalId, ownerId);
        validate(form);
        if (form.getTargetAmount() < current.getCurrentAmount())
            throw new IllegalArgumentException("목표 금액은 현재까지 모은 금액보다 작을 수 없습니다.");
        current.setGoalTitle(form.getGoalTitle().trim());
        current.setTargetAmount(form.getTargetAmount());
        current.setEndDate(form.getEndDate());
        if (goalDao.updateOwned(current) == 0) throw new IllegalArgumentException("저축 목표를 수정하지 못했습니다.");
    }
    @Override public void deposit(Long goalId, String ownerId, long amount) {
        if (amount <= 0) throw new IllegalArgumentException("적립 금액은 1원 이상이어야 합니다.");
        Goal goal = getGoal(goalId, ownerId);
        if (amount > goal.getTargetAmount() - goal.getCurrentAmount())
            throw new IllegalArgumentException("적립 금액이 남은 목표 금액보다 큽니다.");
        if (goalDao.addAmount(goalId, ownerId, amount) == 0)
            throw new IllegalArgumentException("목표 금액을 초과했거나 저축 목표가 변경되었습니다. 다시 확인해 주세요.");
    }
    @Override public void withdraw(Long goalId, String ownerId, long amount) {
        if (amount <= 0) throw new IllegalArgumentException("출금 금액은 1원 이상이어야 합니다.");
        getGoal(goalId, ownerId);
        if (goalDao.subtractAmount(goalId, ownerId, amount) == 0)
            throw new IllegalArgumentException("현재 모은 금액보다 많이 출금할 수 없습니다.");
    }
    @Override public void deleteGoal(Long goalId, String ownerId) {
        if (goalDao.deleteOwned(goalId, ownerId) == 0) throw new IllegalArgumentException("저축 목표를 찾을 수 없습니다.");
    }

    private void validate(Goal goal) {
        if (goal.getGoalTitle() == null || goal.getGoalTitle().isBlank())
            throw new IllegalArgumentException("저축 목표 이름을 입력해 주세요.");
        if (goal.getGoalTitle().trim().length() > 50)
            throw new IllegalArgumentException("저축 목표 이름은 50자 이내로 입력해 주세요.");
        if (goal.getTargetAmount() <= 0)
            throw new IllegalArgumentException("목표 금액은 1원 이상이어야 합니다.");
        if (goal.getEndDate() == null)
            throw new IllegalArgumentException("목표 기한을 입력해 주세요.");
        goal.setGoalTitle(goal.getGoalTitle().trim());
    }
}
