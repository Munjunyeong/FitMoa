package com.asset.flow.account;

import java.time.LocalDate;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountServiceImpl implements AccountService {

	private final AccountDao accountDao;

	public AccountServiceImpl(AccountDao accountDao) {
		this.accountDao = accountDao;
	}

	@Override
	@Transactional(readOnly = true)
	public Account getAccount(String id) {
		return accountDao.findById(id);
	}

	@Override
	@Transactional
	public void deleteAccount(String id) {
		getAccount(id);
		accountDao.deleteById(id);
	}

	@Override
	@Transactional
	public void updateAccount(String id, Account accountForm) {
		Account existingAccount = getAccount(id);
		int updatedRows = accountDao.updateAccount(existingAccount, accountForm);
		if (updatedRows == 0) {
			throw new IllegalStateException("계정 정보 수정에 실패했습니다.");
		}
	}

	@Override
	@Transactional
	public void register(Account item) {
		if (item.getCreatedAt() == null) {
			item.setCreatedAt(LocalDate.now());
		}
		accountDao.register(item);
	}

	@Override
	public boolean login(Account item) {
		Account account = accountDao.item(item.getId());
		if (account != null && account.getPassword().equals(item.getPassword())) {
			BeanUtils.copyProperties(account, item);
			item.setPassword(null);
			return true;
		}
		return false;
	}

}