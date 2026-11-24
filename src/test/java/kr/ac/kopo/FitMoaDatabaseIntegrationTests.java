package kr.ac.kopo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.asset.flow.FitMoaApplication;
import com.asset.flow.account.Account;
import com.asset.flow.account.AccountService;
import com.asset.flow.bank.AccountHistory;
import com.asset.flow.bank.BankAccount;
import com.asset.flow.bank.BankService;
import com.asset.flow.expense.Expense;
import com.asset.flow.expense.ExpenseCategorySummary;
import com.asset.flow.expense.ExpenseService;
import com.asset.flow.goal.Goal;
import com.asset.flow.goal.GoalService;

@SpringBootTest(classes = FitMoaApplication.class)
@Transactional
class FitMoaDatabaseIntegrationTests {

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private AccountService accountService;
    @Autowired private BankService bankService;
    @Autowired private ExpenseService expenseService;
    @Autowired private GoalService goalService;

    @Test
    void configuredSchemaMatchesTheTablesAndGeneratedKeysUsedByTheApp() {
        List<Map<String, Object>> tables = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME, ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE()");
        Set<String> tableNames = new HashSet<>();
        for (Map<String, Object> table : tables) {
            tableNames.add(String.valueOf(table.get("TABLE_NAME")).toLowerCase());
            assertEquals("InnoDB", table.get("ENGINE"), "Transaction tests require InnoDB tables");
        }
        assertTrue(tableNames.containsAll(Set.of("account", "bank_account", "account_history", "expense_category", "savings_goal")));

        Integer passwordLength = jdbcTemplate.queryForObject(
                "SELECT CHARACTER_MAXIMUM_LENGTH FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'account' AND COLUMN_NAME = 'PASSWORD'",
                Integer.class);
        assertNotNull(passwordLength);
        assertTrue(passwordLength >= 60, "ACCOUNT.PASSWORD must fit BCrypt hashes; run database/migrations/002_account_password_hash.sql");
    }

    @Test
    void accountRegistrationLoginAndProfileUpdatesPersist() {
        String ownerId = createUser();
        String storedPassword = jdbcTemplate.queryForObject(
                "SELECT PASSWORD FROM ACCOUNT WHERE ID = ?", String.class, ownerId);
        assertNotNull(storedPassword);
        assertTrue(storedPassword.startsWith("$2"));

        Account login = new Account();
        login.setId(ownerId);
        login.setPassword("testPassword123");
        assertTrue(accountService.login(login));
        assertEquals(ownerId, login.getId());
        assertNull(login.getPassword());

        Account update = new Account();
        update.setName("변경이름");
        update.setPhone("01098765432");
        update.setPassword("newPassword123");
        accountService.updateAccount(ownerId, update);

        Account profile = accountService.getAccount(ownerId);
        assertEquals("변경이름", profile.getName());
        assertEquals("01098765432", profile.getPhone());
        assertNull(profile.getPassword(), "Profile lookup must not load the password hash");

        Account newLogin = new Account();
        newLogin.setId(ownerId);
        newLogin.setPassword("newPassword123");
        assertTrue(accountService.login(newLogin));
        Account oldLogin = new Account();
        oldLogin.setId(ownerId);
        oldLogin.setPassword("testPassword123");
        assertFalse(accountService.login(oldLogin));
    }

    @Test
    void bankTransfersUpdateBothBalancesAndWriteBothHistoryRows() {
        String ownerId = createUser();
        BankAccount from = createBankAccount(ownerId, "출금 통장", 500_000L);
        BankAccount to = createBankAccount(ownerId, "입금 통장", 100_000L);

        bankService.transfer(ownerId, from.getBankAccountId(), to.getBankAccountId(), 75_000L, "DB 연동 테스트");

        assertEquals(425_000L, bankService.getAccount(from.getBankAccountId(), ownerId).getBalance());
        assertEquals(175_000L, bankService.getAccount(to.getBankAccountId(), ownerId).getBalance());
        List<AccountHistory> fromHistory = bankService.getHistory(from.getBankAccountId(), ownerId);
        List<AccountHistory> toHistory = bankService.getHistory(to.getBankAccountId(), ownerId);
        assertEquals(1, fromHistory.size());
        assertEquals(1, toHistory.size());
        assertEquals("TRANSFER_OUT", fromHistory.get(0).getTxType());
        assertEquals("TRANSFER_IN", toHistory.get(0).getTxType());
        assertEquals(425_000L, fromHistory.get(0).getBalanceAfter());
        assertEquals("DB 연동 테스트", fromHistory.get(0).getTxMemo());

        assertThrows(IllegalArgumentException.class,
                () -> bankService.transfer(ownerId, from.getBankAccountId(), to.getBankAccountId(), 999_999L, null));
        assertThrows(IllegalArgumentException.class,
                () -> bankService.transfer(ownerId, from.getBankAccountId(), from.getBankAccountId(), 1L, null));
        assertEquals(425_000L, bankService.getAccount(from.getBankAccountId(), ownerId).getBalance());
    }

    @Test
    void expenseCrudAndMonthlyCategoryTotalsUseDatabaseRows() {
        String ownerId = createUser();
        LocalDate today = LocalDate.now();
        Expense first = new Expense();
        first.setCategoryName("식비");
        first.setAmount(12_000L);
        first.setSpentAt(today);
        expenseService.createExpense(ownerId, first);
        assertNotNull(first.getExpenseId());

        Expense second = new Expense();
        second.setCategoryName("식비");
        second.setAmount(8_000L);
        second.setSpentAt(today.minusDays(1));
        expenseService.createExpense(ownerId, second);

        List<Expense> monthly = expenseService.getMonthlyExpenses(ownerId, YearMonth.from(today));
        assertEquals(2, monthly.size());
        List<ExpenseCategorySummary> summary = expenseService.getCategorySummary(ownerId, YearMonth.from(today));
        assertEquals(1, summary.size());
        assertEquals("식비", summary.get(0).getCategoryName());
        assertEquals(20_000L, summary.get(0).getTotalAmount());

        Expense update = new Expense();
        update.setCategoryName("교통");
        update.setAmount(5_000L);
        update.setSpentAt(today);
        expenseService.updateExpense(first.getExpenseId(), ownerId, update);
        assertEquals("교통", expenseService.getExpense(first.getExpenseId(), ownerId).getCategoryName());

        expenseService.deleteExpense(second.getExpenseId(), ownerId);
        assertEquals(1, expenseService.getMonthlyExpenses(ownerId, YearMonth.from(today)).size());
        assertThrows(IllegalArgumentException.class, () -> expenseService.getExpense(second.getExpenseId(), ownerId));
    }

    @Test
    void goalDepositsAndWithdrawalsMoveBankBalanceAndWriteHistoryAtomically() {
        String ownerId = createUser();
        BankAccount source = createBankAccount(ownerId, "저축 출금 통장", 300_000L);
        BankAccount destination = createBankAccount(ownerId, "저축 입금 통장", 20_000L);
        Goal goal = new Goal();
        goal.setGoalTitle("DB 통합 테스트 목표");
        goal.setTargetAmount(200_000L);
        goal.setEndDate(LocalDate.now().plusDays(30));
        goalService.createGoal(ownerId, goal);
        assertNotNull(goal.getGoalId());

        goalService.deposit(goal.getGoalId(), ownerId, source.getBankAccountId(), 80_000L);
        assertEquals(80_000L, goalService.getGoal(goal.getGoalId(), ownerId).getCurrentAmount());
        assertEquals(220_000L, bankService.getAccount(source.getBankAccountId(), ownerId).getBalance());

        goalService.withdraw(goal.getGoalId(), ownerId, destination.getBankAccountId(), 30_000L);
        assertEquals(50_000L, goalService.getGoal(goal.getGoalId(), ownerId).getCurrentAmount());
        assertEquals(50_000L, bankService.getAccount(destination.getBankAccountId(), ownerId).getBalance());
        assertEquals("GOAL_DEPOSIT", bankService.getHistory(source.getBankAccountId(), ownerId).get(0).getTxType());
        assertEquals("GOAL_WITHDRAW", bankService.getHistory(destination.getBankAccountId(), ownerId).get(0).getTxType());

        assertThrows(IllegalArgumentException.class,
                () -> goalService.deposit(goal.getGoalId(), ownerId, source.getBankAccountId(), 200_000L));
        assertThrows(IllegalArgumentException.class,
                () -> goalService.withdraw(goal.getGoalId(), ownerId, destination.getBankAccountId(), 60_000L));
        assertEquals(50_000L, goalService.getGoal(goal.getGoalId(), ownerId).getCurrentAmount());
        assertEquals(220_000L, bankService.getAccount(source.getBankAccountId(), ownerId).getBalance());
        assertEquals(50_000L, bankService.getAccount(destination.getBankAccountId(), ownerId).getBalance());
    }

    private String createUser() {
        Account account = new Account();
        account.setId("it-" + UUID.randomUUID());
        account.setName("통합테스트");
        account.setPhone("01012345678");
        account.setPassword("testPassword123");
        accountService.register(account);
        return account.getId();
    }

    private BankAccount createBankAccount(String ownerId, String name, long balance) {
        BankAccount account = new BankAccount();
        account.setBankName(name);
        account.setAccountType("CHECKING");
        account.setBalance(balance);
        bankService.createAccount(ownerId, account);
        return account;
    }
}
