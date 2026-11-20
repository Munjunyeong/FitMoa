package com.asset.flow.account;

public interface AccountService {

	Account getAccount(String id);

	void updateAccount(String id, Account accountForm);

	void deleteAccount(String id);

	void register(Account item);

	boolean login(Account item);

}