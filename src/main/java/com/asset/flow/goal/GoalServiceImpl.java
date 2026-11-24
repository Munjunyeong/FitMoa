package com.asset.flow.goal;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asset.flow.bank.AccountHistory;
import com.asset.flow.bank.BankAccount;
import com.asset.flow.bank.BankDao;

@Service
public class GoalServiceImpl implements GoalService {
    private final GoalDao goalDao;
    private final BankDao bankDao;
    public GoalServiceImpl(GoalDao goalDao, BankDao bankDao) { this.goalDao = goalDao; this.bankDao = bankDao; }

    @Override @Transactional(readOnly = true)
    public List<Goal> getGoals(String ownerId) { return goalDao.findAllByOwner(ownerId); }
    @Transactional(readOnly = true)
    @Override public Goal getGoal(Long goalId, String ownerId) {
        Goal goal = goalDao.findOwned(goalId, ownerId);
        if (goal == null) throw new IllegalArgumentException("저축 목표를 찾을 수 없습니다.");
        return goal;
    }
    @Transactional
    @Override public void createGoal(String ownerId, Goal goal) {
        validate(goal);
        goal.setId(ownerId);
        goal.setCurrentAmount(0);
        goalDao.insert(goal);
    }
    @Transactional
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
    @Transactional
    @Override public void deposit(Long goalId, String ownerId, String bankAccountId, long amount) {
        if (amount <= 0) throw new IllegalArgumentException("적립 금액은 1원 이상이어야 합니다.");
        Goal goal = getGoal(goalId, ownerId);
        if (amount > goal.getTargetAmount() - goal.getCurrentAmount())
            throw new IllegalArgumentException("적립 금액이 남은 목표 금액보다 큽니다.");
        BankAccount account = ownedBankAccount(bankAccountId, ownerId);
        if (account.getBalance() < amount) throw new IllegalArgumentException("선택한 계좌의 잔액이 부족합니다.");
        if (bankDao.debit(bankAccountId, ownerId, amount) != 1)
            throw new IllegalArgumentException("계좌 잔액이 부족하거나 변경되었습니다. 다시 확인해 주세요.");
        if (goalDao.addAmount(goalId, ownerId, amount) == 0)
            throw new IllegalArgumentException("목표 금액을 초과했거나 저축 목표가 변경되었습니다. 다시 확인해 주세요.");
        saveHistory(account, goal, "GOAL_DEPOSIT", amount, account.getBalance() - amount);
    }
    @Transactional
    @Override public void withdraw(Long goalId, String ownerId, String bankAccountId, long amount) {
        if (amount <= 0) throw new IllegalArgumentException("출금 금액은 1원 이상이어야 합니다.");
        Goal goal = getGoal(goalId, ownerId);
        BankAccount account = ownedBankAccount(bankAccountId, ownerId);
        if (amount > Long.MAX_VALUE - account.getBalance()) throw new IllegalArgumentException("계좌 잔액이 허용 범위를 초과합니다.");
        if (goalDao.subtractAmount(goalId, ownerId, amount) == 0)
            throw new IllegalArgumentException("현재 모은 금액보다 많이 출금할 수 없습니다.");
        if (bankDao.credit(bankAccountId, ownerId, amount) != 1)
            throw new IllegalArgumentException("입금 계좌 잔액을 갱신하지 못했습니다.");
        saveHistory(account, goal, "GOAL_WITHDRAW", amount, account.getBalance() + amount);
    }
    @Transactional
    @Override public void deleteGoal(Long goalId, String ownerId) {
        if (goalDao.deleteOwned(goalId, ownerId) == 0) throw new IllegalArgumentException("저축 목표를 찾을 수 없습니다.");
    }

    private BankAccount ownedBankAccount(String bankAccountId, String ownerId) {
        if (bankAccountId == null || bankAccountId.isBlank())
            throw new IllegalArgumentException("거래할 계좌를 선택해 주세요.");
        BankAccount account = bankDao.findOwned(bankAccountId, ownerId);
        if (account == null) throw new IllegalArgumentException("선택한 계좌를 찾을 수 없습니다.");
        return account;
    }

    private void saveHistory(BankAccount account, Goal goal, String type, long amount, long balanceAfter) {
        AccountHistory history = new AccountHistory();
        history.setBankAccountId(account.getBankAccountId());
        history.setTxType(type);
        history.setAmount(amount);
        history.setCounterpartyAccountId("GOAL-" + goal.getGoalId());
        history.setBalanceAfter(balanceAfter);
        history.setTxMemo((type.equals("GOAL_DEPOSIT") ? "저축 목표 적립: " : "저축 목표 출금: ") + goal.getGoalTitle());
        history.setCreatedAt(LocalDateTime.now());
        if (bankDao.insertHistory(history) != 1) throw new IllegalStateException("거래 내역을 저장하지 못했습니다.");
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
