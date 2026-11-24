package com.asset.flow.bank;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

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
		validate(form, true);
		form.setBankAccountId(UUID.randomUUID().toString());
		form.setId(ownerId);
		if (bankDao.insert(form) != 1)
			throw new IllegalStateException("계좌 등록에 실패했습니다.");
	}

	@Override
	@Transactional
	public void updateAccount(String bankAccountId, String ownerId, BankAccount form) {
		validate(form, false);
		BankAccount existing = getAccount(bankAccountId, ownerId);
		form.setBankAccountId(bankAccountId);
		form.setId(ownerId);
		form.setBalance(existing.getBalance());
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

	@Override
	@Transactional(readOnly = true)
	public List<AccountHistory> getHistory(String bankAccountId, String ownerId) {
		getAccount(bankAccountId, ownerId);
		return bankDao.findHistory(bankAccountId, ownerId);
	}

	@Override
	@Transactional
	public void transfer(String ownerId, String fromAccountId, String toAccountId, long amount, String memo) {
		if (amount <= 0) throw new IllegalArgumentException("이체 금액은 1원 이상이어야 합니다.");
		if (fromAccountId == null || fromAccountId.isBlank() || toAccountId == null || toAccountId.isBlank())
			throw new IllegalArgumentException("출금 계좌와 입금 계좌를 선택해 주세요.");
		if (fromAccountId.equals(toAccountId)) throw new IllegalArgumentException("출금 계좌와 입금 계좌는 달라야 합니다.");
		if (memo != null && memo.length() > 250) throw new IllegalArgumentException("메모는 250자 이내로 입력해 주세요.");

		BankAccount from = getAccount(fromAccountId, ownerId);
		BankAccount to = getAccount(toAccountId, ownerId);
		if (from.getBalance() < amount) throw new IllegalArgumentException("출금 계좌의 잔액이 부족합니다.");
		if (bankDao.debit(fromAccountId, ownerId, amount) != 1)
			throw new IllegalArgumentException("출금 계좌 잔액이 부족하거나 변경되었습니다. 다시 확인해 주세요.");
		if (bankDao.credit(toAccountId, ownerId, amount) != 1)
			throw new IllegalArgumentException("입금 계좌를 갱신하지 못했습니다.");

		long fromBalance = from.getBalance() - amount;
		long toBalance;
		try { toBalance = Math.addExact(to.getBalance(), amount); }
		catch (ArithmeticException e) { throw new IllegalArgumentException("입금 계좌 잔액이 허용 범위를 초과합니다."); }
		insertHistory(fromAccountId, "TRANSFER_OUT", amount, toAccountId, fromBalance, memo);
		insertHistory(toAccountId, "TRANSFER_IN", amount, fromAccountId, toBalance, memo);
	}

	private void insertHistory(String accountId, String txType, long amount, String counterpartyId,
			long balanceAfter, String memo) {
		AccountHistory history = new AccountHistory();
		history.setBankAccountId(accountId);
		history.setTxType(txType);
		history.setAmount(amount);
		history.setCounterpartyAccountId(counterpartyId);
		history.setBalanceAfter(balanceAfter);
		history.setTxMemo(memo);
		history.setCreatedAt(LocalDateTime.now());
		if (bankDao.insertHistory(history) != 1) throw new IllegalStateException("거래 내역을 저장하지 못했습니다.");
	}

	private void validate(BankAccount form, boolean requireBalance) {
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
		if (requireBalance && (form.getBalance() == null || form.getBalance() < 0)) {
			throw new IllegalArgumentException("잔액은 0 이상의 금액으로 입력해 주세요.");
		}
		form.setBankName(form.getBankName().trim());
		form.setAccountType(form.getAccountType().trim());
	}
}
