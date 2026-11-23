package com.asset.flow.expense;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/expenses")
public class ExpenseController {
    private static final String LOGIN_ACCOUNT_ID_KEY = "LOGIN_ACCOUNT_ID";
    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public String list(HttpSession session,
                       @RequestParam(name = "month", required = false) String monthParam,
                       Model model) {
        String ownerId = ownerId(session);
        if (ownerId == null) return "redirect:/login";

        YearMonth month = parseMonth(monthParam);
        List<Expense> expenses = expenseService.getMonthlyExpenses(ownerId, month);
        List<ExpenseCategorySummary> categories = expenseService.getCategorySummary(ownerId, month);
        long total = expenses.stream().mapToLong(Expense::getAmount).sum();
        int elapsedDays = elapsedDays(month);
        for (ExpenseCategorySummary category : categories) {
            int percentage = total == 0 ? 0 : (int) Math.round(category.getTotalAmount() * 100.0 / total);
            category.setPercentage(percentage);
        }

        ExpenseCategorySummary topCategory = categories.isEmpty() ? null : categories.get(0);
        model.addAttribute("expenses", expenses);
        model.addAttribute("categories", categories);
        model.addAttribute("month", month.toString());
        model.addAttribute("monthLabel", month.getYear() + "년 " + month.getMonthValue() + "월");
        model.addAttribute("previousMonth", month.minusMonths(1));
        model.addAttribute("nextMonth", month.plusMonths(1));
        model.addAttribute("totalAmount", total);
        model.addAttribute("dailyAverage", total / elapsedDays);
        model.addAttribute("expenseCount", expenses.size());
        model.addAttribute("topCategory", topCategory);
        return "expenses";
    }

    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        if (ownerId(session) == null) return "redirect:/login";
        Expense expense = new Expense();
        expense.setSpentAt(LocalDate.now());
        model.addAttribute("expense", expense);
        model.addAttribute("pageTitle", "지출 등록");
        model.addAttribute("formAction", "/expenses");
        model.addAttribute("month", YearMonth.now().toString());
        return "expense-form";
    }

    @PostMapping
    public String create(HttpSession session, @ModelAttribute Expense expense, RedirectAttributes redirect) {
        String ownerId = ownerId(session);
        if (ownerId == null) return "redirect:/login";
        try {
            expenseService.createExpense(ownerId, expense);
            redirect.addFlashAttribute("message", "지출 내역을 등록했습니다.");
            return "redirect:/expenses?month=" + YearMonth.from(expense.getSpentAt());
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/expenses/new";
        }
    }

    @GetMapping("/{expenseId}/edit")
    public String editForm(@PathVariable Long expenseId, HttpSession session, Model model,
                           RedirectAttributes redirect) {
        String ownerId = ownerId(session);
        if (ownerId == null) return "redirect:/login";
        try {
            Expense expense = expenseService.getExpense(expenseId, ownerId);
            model.addAttribute("expense", expense);
            model.addAttribute("pageTitle", "지출 수정");
            model.addAttribute("formAction", "/expenses/" + expenseId + "/edit");
            model.addAttribute("month", YearMonth.from(expense.getSpentAt()).toString());
            return "expense-form";
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/expenses";
        }
    }

    @PostMapping("/{expenseId}/edit")
    public String update(@PathVariable Long expenseId, HttpSession session,
                         @ModelAttribute Expense expense, RedirectAttributes redirect) {
        String ownerId = ownerId(session);
        if (ownerId == null) return "redirect:/login";
        try {
            expenseService.updateExpense(expenseId, ownerId, expense);
            redirect.addFlashAttribute("message", "지출 내역을 수정했습니다.");
            return "redirect:/expenses?month=" + YearMonth.from(expense.getSpentAt());
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/expenses/" + expenseId + "/edit";
        }
    }

    @PostMapping("/{expenseId}/delete")
    public String delete(@PathVariable Long expenseId, HttpSession session,
                         @RequestParam(name = "month", required = false) String monthParam,
                         RedirectAttributes redirect) {
        String ownerId = ownerId(session);
        if (ownerId == null) return "redirect:/login";
        try {
            expenseService.deleteExpense(expenseId, ownerId);
            redirect.addFlashAttribute("message", "지출 내역을 삭제했습니다.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/expenses?month=" + parseMonth(monthParam);
    }

    private String ownerId(HttpSession session) {
        return (String) session.getAttribute(LOGIN_ACCOUNT_ID_KEY);
    }

    private YearMonth parseMonth(String monthParam) {
        if (monthParam == null || monthParam.isBlank()) return YearMonth.now();
        try {
            return YearMonth.parse(monthParam);
        } catch (DateTimeException e) {
            return YearMonth.now();
        }
    }

    private int elapsedDays(YearMonth month) {
        YearMonth currentMonth = YearMonth.now();
        if (month.isAfter(currentMonth)) return 1;
        return month.equals(currentMonth) ? LocalDate.now().getDayOfMonth() : month.lengthOfMonth();
    }
}
