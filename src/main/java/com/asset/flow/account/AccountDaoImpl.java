package com.asset.flow.account;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AccountDaoImpl implements AccountDao {

	private static final String NAMESPACE = "com.asset.flow.account.AccountMapper.";

	private final SqlSessionTemplate sqlSession;

	public AccountDaoImpl(SqlSessionTemplate sqlSession) {
		this.sqlSession = sqlSession;
	}

	@Override
	public void deleteById(String id) {
		sqlSession.delete(NAMESPACE + "deleteById", id);
	}

	@Override
	public Account findById(String id) {
		return sqlSession.selectOne(NAMESPACE + "findById", id);
	}

	@Override
	public int updateAccount(Account account, Account accountForm) {
		if (accountForm.getName() != null && !accountForm.getName().isBlank()) {
			account.setName(accountForm.getName());
		}
		if (accountForm.getPhone() != null && !accountForm.getPhone().isBlank()) {
			account.setPhone(accountForm.getPhone());
		}
		if (accountForm.getPassword() != null && !accountForm.getPassword().isBlank()) {
			account.setPassword(accountForm.getPassword());
		}
		return sqlSession.update(NAMESPACE + "updateAccount", account);
	}

	@Override
	public void register(Account item) {
		sqlSession.insert(NAMESPACE + "register", item);
	}

	@Override
	public Account item(String id) {
		return sqlSession.selectOne(NAMESPACE + "item", id);
	}

}