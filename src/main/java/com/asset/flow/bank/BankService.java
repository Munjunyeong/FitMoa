package com.asset.flow.bank;

import java.util.List;

public interface BankService {
	List<BankAccount> getAccounts(String ownerId);

	BankAccount getAccount(String bankAccountId, String ownerId);

	void createAccount(String ownerId, BankAccount form);

	void updateAccount(String bankAccountId, String ownerId, BankAccount form);

	void deleteAccount(String bankAccountId, String ownerId);

	List<AccountHistory> getHistory(String bankAccountId, String ownerId);

	void transfer(String ownerId, String fromAccountId, String toAccountId, long amount, String memo);
}
