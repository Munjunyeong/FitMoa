package com.asset.flow.expense;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseServiceImpl implements ExpenseService {
    private final ExpenseDao expenseDao;
    public ExpenseServiceImpl(ExpenseDao expenseDao) { this.expenseDao = expenseDao; }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getMonthlyExpenses(String ownerId, YearMonth month) {
        return expenseDao.findByOwnerAndPeriod(ownerId, month.atDay(1), month.plusMonths(1).atDay(1));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseCategorySummary> getCategorySummary(String ownerId, YearMonth month) {
        return expenseDao.summarizeByCategory(ownerId, month.atDay(1), month.plusMonths(1).atDay(1));
    }

    @Override
    @Transactional(readOnly = true)
    public Expense getExpense(Long expenseId, String ownerId) {
        Expense expense = expenseDao.findOwned(expenseId, ownerId);
        if (expense == null) throw new IllegalArgumentException("지출 내역을 찾을 수 없습니다.");
        return expense;
    }

    @Override
    @Transactional
    public void createExpense(String ownerId, Expense expense) {
        if (expense.getSpentAt() == null) expense.setSpentAt(LocalDate.now());
        validate(expense);
        expense.setId(ownerId);
        if (expenseDao.insert(expense) != 1) throw new IllegalStateException("지출 등록에 실패했습니다.");
    }

    @Override
    @Transactional
    public void updateExpense(Long expenseId, String ownerId, Expense expense) {
        validate(expense);
        getExpense(expenseId, ownerId);
        expense.setExpenseId(expenseId);
        expense.setId(ownerId);
        if (expenseDao.updateOwned(expense) != 1) throw new IllegalStateException("지출 수정에 실패했습니다.");
    }

    @Override
    @Transactional
    public void deleteExpense(Long expenseId, String ownerId) {
        getExpense(expenseId, ownerId);
        if (expenseDao.deleteOwned(expenseId, ownerId) != 1) throw new IllegalStateException("지출 삭제에 실패했습니다.");
    }

    private void validate(Expense expense) {
        if (expense.getCategoryName() == null || expense.getCategoryName().isBlank())
            throw new IllegalArgumentException("카테고리를 입력해 주세요.");
        expense.setCategoryName(expense.getCategoryName().trim());
        if (expense.getCategoryName().length() > 50)
            throw new IllegalArgumentException("카테고리는 50자 이내로 입력해 주세요.");
        if (expense.getAmount() == null || expense.getAmount() <= 0)
            throw new IllegalArgumentException("금액은 1원 이상 입력해 주세요.");
        if (expense.getSpentAt() == null)
            throw new IllegalArgumentException("지출 날짜를 입력해 주세요.");
    }
}
