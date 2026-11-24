package com.asset.flow.bank;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BankDaoImpl implements BankDao {
	private static final String NAMESPACE = "com.asset.flow.bank.BankMapper.";
	private final SqlSessionTemplate sqlSession;

	public BankDaoImpl(SqlSessionTemplate sqlSession) {
		this.sqlSession = sqlSession;
	}

	@Override
	public List<BankAccount> findAllByOwner(String ownerId) {
		return sqlSession.selectList(NAMESPACE + "findAllByOwner", ownerId);
	}

	@Override
	public BankAccount findOwned(String bankAccountId, String ownerId) {
		return sqlSession.selectOne(NAMESPACE + "findOwned", params(bankAccountId, ownerId));
	}

	@Override
	public int insert(BankAccount bankAccount) {
		return sqlSession.insert(NAMESPACE + "insert", bankAccount);
	}

	@Override
	public int updateOwned(BankAccount bankAccount) {
		return sqlSession.update(NAMESPACE + "updateOwned", bankAccount);
	}

	@Override
	public int deleteOwned(String bankAccountId, String ownerId) {
		return sqlSession.delete(NAMESPACE + "deleteOwned", params(bankAccountId, ownerId));
	}

	@Override
	public List<AccountHistory> findHistory(String bankAccountId, String ownerId) {
		return sqlSession.selectList(NAMESPACE + "findHistory", params(bankAccountId, ownerId));
	}

	@Override
	public int debit(String bankAccountId, String ownerId, long amount) {
		return sqlSession.update(NAMESPACE + "debit", amountParams(bankAccountId, ownerId, amount));
	}

	@Override
	public int credit(String bankAccountId, String ownerId, long amount) {
		return sqlSession.update(NAMESPACE + "credit", amountParams(bankAccountId, ownerId, amount));
	}

	@Override
	public int insertHistory(AccountHistory history) {
		return sqlSession.insert(NAMESPACE + "insertHistory", history);
	}

	private Map<String, Object> amountParams(String bankAccountId, String ownerId, long amount) {
		Map<String, Object> params = new HashMap<>();
		params.put("bankAccountId", bankAccountId);
		params.put("ownerId", ownerId);
		params.put("amount", amount);
		return params;
	}

	private BankAccountKey params(String bankAccountId, String ownerId) {
		return new BankAccountKey(bankAccountId, ownerId);
	}

	public static class BankAccountKey {
		private final String bankAccountId;
		private final String ownerId;

		public BankAccountKey(String bankAccountId, String ownerId) {
			this.bankAccountId = bankAccountId;
			this.ownerId = ownerId;
		}

		public String getBankAccountId() {
			return bankAccountId;
		}

		public String getOwnerId() {
			return ownerId;
		}
	}
}
