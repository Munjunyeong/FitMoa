package com.asset.flow.account;

public interface AccountDao {

	int updateAccount(Account account, Account accountForm);

	void deleteById(String id);

	Account findById(String id);

	void register(Account item);

	Account item(String id);

	int updatePassword(String id, String passwordHash);

}
