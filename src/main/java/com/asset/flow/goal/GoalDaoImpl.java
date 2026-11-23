package com.asset.flow.goal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class GoalDaoImpl implements GoalDao {
    private static final String NS = "com.asset.flow.goal.GoalMapper.";
    private final SqlSessionTemplate sqlSession;

    public GoalDaoImpl(SqlSessionTemplate sqlSession) { this.sqlSession = sqlSession; }

    @Override public List<Goal> findAllByOwner(String ownerId) {
        return sqlSession.selectList(NS + "findAllByOwner", ownerId);
    }
    @Override public Goal findOwned(Long goalId, String ownerId) {
        return sqlSession.selectOne(NS + "findOwned", params(goalId, ownerId, 0));
    }
    @Override public int insert(Goal goal) { return sqlSession.insert(NS + "insert", goal); }
    @Override public int updateOwned(Goal goal) { return sqlSession.update(NS + "updateOwned", goal); }
    @Override public int addAmount(Long goalId, String ownerId, long amount) {
        return sqlSession.update(NS + "addAmount", params(goalId, ownerId, amount));
    }
    @Override public int subtractAmount(Long goalId, String ownerId, long amount) {
        return sqlSession.update(NS + "subtractAmount", params(goalId, ownerId, amount));
    }
    @Override public int deleteOwned(Long goalId, String ownerId) {
        return sqlSession.delete(NS + "deleteOwned", params(goalId, ownerId, 0));
    }

    private Map<String, Object> params(Long goalId, String ownerId, long amount) {
        Map<String, Object> p = new HashMap<>();
        p.put("goalId", goalId); p.put("ownerId", ownerId); p.put("amount", amount);
        return p;
    }
}
