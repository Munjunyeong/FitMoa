package com.asset.flow.bank;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BankServiceImpl implements BankService {
	private final BankDao bankDao;

	public BankServiceImpl(BankDao bankDao) {
		this.bankDao = bankDao;
	}

	@Override
	@Transactional(readOnly = true)
	public List<BankAccount> getAccounts(String ownerId) {
		return bankDao.findAllByOwner(ownerId);
	}

	@Override
	@Transactional(readOnly = true)
	public BankAccount getAccount(String bankAccountId, String ownerId) {
		BankAccount account = bankDao.findOwned(bankAccountId, ownerId);
		if (account == null)
			throw new IllegalArgumentException("계좌를 찾을 수 없습니다.");
		return account;
	}

	@Override
	@Transactional
	public void createAccount(String ownerId, BankAccount form) {
		validate(form);
		form.setBankAccountId(UUID.randomUUID().toString());
		form.setId(ownerId);
		if (bankDao.insert(form) != 1)
			throw new IllegalStateException("계좌 등록에 실패했습니다.");
	}

	@Override
	@Transactional
	public void updateAccount(String bankAccountId, String ownerId, BankAccount form) {
		validate(form);
		getAccount(bankAccountId, ownerId);
		form.setBankAccountId(bankAccountId);
		form.setId(ownerId);
		if (bankDao.updateOwned(form) != 1)
			throw new IllegalStateException("계좌 수정에 실패했습니다.");
	}

	@Override
	@Transactional
	public void deleteAccount(String bankAccountId, String ownerId) {
		getAccount(bankAccountId, ownerId);
		if (bankDao.deleteOwned(bankAccountId, ownerId) != 1)
			throw new IllegalStateException("계좌 삭제에 실패했습니다.");
	}

	private void validate(BankAccount form) {
		if (form.getBankName() == null || form.getBankName().isBlank()) {
			throw new IllegalArgumentException("은행명을 입력해 주세요.");
		}
		if (form.getBankName().length() > 50)
			throw new IllegalArgumentException("은행명은 50자 이내로 입력해 주세요.");
		if (form.getAccountType() == null || form.getAccountType().isBlank()) {
			throw new IllegalArgumentException("계좌 유형을 입력해 주세요.");
		}
		if (form.getAccountType().length() > 50)
			throw new IllegalArgumentException("계좌 유형은 50자 이내로 입력해 주세요.");
		if (form.getBalance() == null || form.getBalance() < 0) {
			throw new IllegalArgumentException("잔액은 0 이상의 금액으로 입력해 주세요.");
		}
	}
}
