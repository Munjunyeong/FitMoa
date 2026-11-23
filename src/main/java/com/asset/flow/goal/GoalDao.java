package com.asset.flow.goal;

import java.util.List;

public interface GoalDao {
    List<Goal> findAllByOwner(String ownerId);
    Goal findOwned(Long goalId, String ownerId);
    int insert(Goal goal);
    int updateOwned(Goal goal);
    int addAmount(Long goalId, String ownerId, long amount);
    int subtractAmount(Long goalId, String ownerId, long amount);
    int deleteOwned(Long goalId, String ownerId);
}
