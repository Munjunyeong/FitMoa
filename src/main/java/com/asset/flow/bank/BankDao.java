package com.asset.flow.bank;

import java.util.List;

public interface BankDao {
	List<BankAccount> findAllByOwner(String ownerId);

	BankAccount findOwned(String bankAccountId, String ownerId);

	int insert(BankAccount bankAccount);

	int updateOwned(BankAccount bankAccount);

	int deleteOwned(String bankAccountId, String ownerId);
}
