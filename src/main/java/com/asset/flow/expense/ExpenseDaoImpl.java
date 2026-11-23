package com.asset.flow.expense;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ExpenseDaoImpl implements ExpenseDao {
    private static final String NAMESPACE = "com.asset.flow.expense.ExpenseMapper.";
    private final SqlSessionTemplate sqlSession;

    public ExpenseDaoImpl(SqlSessionTemplate sqlSession) { this.sqlSession = sqlSession; }

    @Override
    public List<Expense> findByOwnerAndPeriod(String ownerId, LocalDate fromDate, LocalDate untilDate) {
        return sqlSession.selectList(NAMESPACE + "findByOwnerAndPeriod", periodParams(ownerId, fromDate, untilDate));
    }

    @Override
    public Expense findOwned(Long expenseId, String ownerId) {
        return sqlSession.selectOne(NAMESPACE + "findOwned", ownerParams(expenseId, ownerId));
    }

    @Override
    public List<ExpenseCategorySummary> summarizeByCategory(String ownerId, LocalDate fromDate, LocalDate untilDate) {
        return sqlSession.selectList(NAMESPACE + "summarizeByCategory", periodParams(ownerId, fromDate, untilDate));
    }

    @Override
    public int insert(Expense expense) { return sqlSession.insert(NAMESPACE + "insert", expense); }

    @Override
    public int updateOwned(Expense expense) { return sqlSession.update(NAMESPACE + "updateOwned", expense); }

    @Override
    public int deleteOwned(Long expenseId, String ownerId) {
        return sqlSession.delete(NAMESPACE + "deleteOwned", ownerParams(expenseId, ownerId));
    }

    private Map<String, Object> periodParams(String ownerId, LocalDate fromDate, LocalDate untilDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("ownerId", ownerId);
        params.put("fromDate", fromDate);
        params.put("untilDate", untilDate);
        return params;
    }

    private Map<String, Object> ownerParams(Long expenseId, String ownerId) {
        Map<String, Object> params = new HashMap<>();
        params.put("expenseId", expenseId);
        params.put("ownerId", ownerId);
        return params;
    }
}
