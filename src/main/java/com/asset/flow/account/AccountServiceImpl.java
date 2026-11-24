package com.asset.flow.account;

import java.time.LocalDate;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AccountServiceImpl implements AccountService {

	private final AccountDao accountDao;
	private final PasswordEncoder passwordEncoder;

	public AccountServiceImpl(AccountDao accountDao, PasswordEncoder passwordEncoder) {
		this.accountDao = accountDao;
		this.passwordEncoder = passwordEncoder;
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
		if (existingAccount == null) throw new IllegalArgumentException("계정을 찾을 수 없습니다.");
		if (accountForm.getName() == null || accountForm.getName().isBlank() || accountForm.getName().trim().length() > 11)
			throw new IllegalArgumentException("이름은 1자 이상 11자 이하로 입력해 주세요.");
		if (accountForm.getPhone() != null && accountForm.getPhone().length() > 13)
			throw new IllegalArgumentException("전화번호는 13자 이내로 입력해 주세요.");
		accountForm.setName(accountForm.getName().trim());
		if (accountForm.getPassword() != null && !accountForm.getPassword().isBlank()) {
			validatePassword(accountForm.getPassword());
			accountForm.setPassword(passwordEncoder.encode(accountForm.getPassword()));
		}
		int updatedRows = accountDao.updateAccount(existingAccount, accountForm);
		if (updatedRows == 0) {
			throw new IllegalStateException("계정 정보 수정에 실패했습니다.");
		}
	}

	@Override
	@Transactional
	public void register(Account item) {
		validateAccount(item);
		if (item.getCreatedAt() == null) {
			item.setCreatedAt(LocalDate.now());
		}
		item.setId(item.getId().trim());
		item.setName(item.getName().trim());
		item.setPassword(passwordEncoder.encode(item.getPassword()));
		accountDao.register(item);
	}

	@Override
	public boolean login(Account item) {
		if (item.getId() == null || item.getId().isBlank() || item.getPassword() == null) return false;
		Account account = accountDao.item(item.getId());
		if (account != null && matchesOrUpgrade(item.getId(), item.getPassword(), account.getPassword())) {
			BeanUtils.copyProperties(account, item);
			item.setPassword(null);
			return true;
		}
		return false;
	}

	private boolean matchesOrUpgrade(String id, String rawPassword, String storedPassword) {
		if (storedPassword == null) return false;
		if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$"))
			return passwordEncoder.matches(rawPassword, storedPassword);
		if (!storedPassword.equals(rawPassword)) return false;
		accountDao.updatePassword(id, passwordEncoder.encode(rawPassword));
		return true;
	}

	private void validateAccount(Account item) {
		if (item.getId() == null || item.getId().isBlank() || item.getId().trim().length() > 50)
			throw new IllegalArgumentException("아이디를 입력해 주세요. (최대 50자)");
		if (item.getName() == null || item.getName().isBlank() || item.getName().trim().length() > 11)
			throw new IllegalArgumentException("이름을 입력해 주세요. (최대 11자)");
		validatePassword(item.getPassword());
		if (item.getPhone() == null || !item.getPhone().matches("^01[016789]-?\\d{3,4}-?\\d{4}$") || item.getPhone().length() > 13)
			throw new IllegalArgumentException("전화번호를 확인해 주세요.");
	}

	private void validatePassword(String password) {
		if (password == null || password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72)
			throw new IllegalArgumentException("비밀번호는 8자 이상 72자 이하로 입력해 주세요.");
	}

}
