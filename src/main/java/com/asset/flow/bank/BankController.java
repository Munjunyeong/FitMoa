package com.asset.flow.bank;

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
@RequestMapping("/bank")
public class BankController {
	private static final String LOGIN_ACCOUNT_ID_KEY = "LOGIN_ACCOUNT_ID";
	private final BankService bankService;

	public BankController(BankService bankService) { this.bankService = bankService; }

	@GetMapping
	public String list(HttpSession session, Model model) {
		String ownerId = ownerId(session);
		if (ownerId == null) return "redirect:/login";
		model.addAttribute("accounts", bankService.getAccounts(ownerId));
		return "bank/list";
	}

	@GetMapping("/new")
	public String createForm(HttpSession session, Model model) {
		if (ownerId(session) == null) return "redirect:/login";
		BankAccount form = new BankAccount();
		form.setBalance(0L);
		model.addAttribute("bankAccount", form);
		model.addAttribute("pageTitle", "계좌 등록");
		model.addAttribute("isNew", true);
		model.addAttribute("formAction", "/bank");
		return "bank/form";
	}

	@PostMapping
	public String create(HttpSession session, @ModelAttribute BankAccount form, RedirectAttributes redirect) {
		String ownerId = ownerId(session);
		if (ownerId == null) return "redirect:/login";
		try {
			bankService.createAccount(ownerId, form);
			redirect.addFlashAttribute("message", "계좌가 등록되었습니다.");
			return "redirect:/bank";
		} catch (IllegalArgumentException e) {
			redirect.addFlashAttribute("error", e.getMessage());
			return "redirect:/bank/new";
		}
	}

	@GetMapping("/{bankAccountId}")
	public String detail(@PathVariable String bankAccountId, HttpSession session, Model model) {
		String ownerId = ownerId(session);
		if (ownerId == null) return "redirect:/login";
		try {
			model.addAttribute("bankAccount", bankService.getAccount(bankAccountId, ownerId));
			model.addAttribute("history", bankService.getHistory(bankAccountId, ownerId));
			return "bank/detail";
		} catch (IllegalArgumentException e) {
			model.addAttribute("bankAccount", null);
			return "bank/detail";
		}
	}

	@GetMapping("/{bankAccountId}/edit")
	public String editForm(@PathVariable String bankAccountId, HttpSession session, Model model) {
		String ownerId = ownerId(session);
		if (ownerId == null) return "redirect:/login";
		try {
			model.addAttribute("bankAccount", bankService.getAccount(bankAccountId, ownerId));
			model.addAttribute("pageTitle", "계좌 수정");
			model.addAttribute("isNew", false);
			model.addAttribute("formAction", "/bank/" + bankAccountId + "/edit");
			return "bank/form";
		} catch (IllegalArgumentException e) {
			return "redirect:/bank";
		}
	}

	@GetMapping("/transfer")
	public String transferForm(HttpSession session, Model model) {
		String ownerId = ownerId(session);
		if (ownerId == null) return "redirect:/login";
		model.addAttribute("accounts", bankService.getAccounts(ownerId));
		return "bank/transfer";
	}

	@PostMapping("/transfer")
	public String transfer(HttpSession session,
			@RequestParam String fromAccountId,
			@RequestParam String toAccountId,
			@RequestParam long amount,
			@RequestParam(required = false) String memo,
			RedirectAttributes redirect) {
		String ownerId = ownerId(session);
		if (ownerId == null) return "redirect:/login";
		try {
			bankService.transfer(ownerId, fromAccountId, toAccountId, amount, memo);
			redirect.addFlashAttribute("message", "계좌 이체가 완료되었습니다.");
			return "redirect:/bank";
		} catch (IllegalArgumentException e) {
			redirect.addFlashAttribute("error", e.getMessage());
			return "redirect:/bank/transfer";
		}
	}

	@PostMapping("/{bankAccountId}/edit")
	public String update(@PathVariable String bankAccountId, HttpSession session,
			@ModelAttribute BankAccount form, RedirectAttributes redirect) {
		String ownerId = ownerId(session);
		if (ownerId == null) return "redirect:/login";
		try {
			bankService.updateAccount(bankAccountId, ownerId, form);
			redirect.addFlashAttribute("message", "계좌 정보가 수정되었습니다.");
			return "redirect:/bank/" + bankAccountId;
		} catch (IllegalArgumentException e) {
			redirect.addFlashAttribute("error", e.getMessage());
			return "redirect:/bank/" + bankAccountId + "/edit";
		}
	}

	@PostMapping("/{bankAccountId}/delete")
	public String delete(@PathVariable String bankAccountId, HttpSession session, RedirectAttributes redirect) {
		String ownerId = ownerId(session);
		if (ownerId == null) return "redirect:/login";
		try {
			bankService.deleteAccount(bankAccountId, ownerId);
			redirect.addFlashAttribute("message", "계좌가 삭제되었습니다.");
		} catch (IllegalArgumentException e) {
			redirect.addFlashAttribute("error", e.getMessage());
		} catch (RuntimeException e) {
			redirect.addFlashAttribute("error", "거래 내역이 연결된 계좌는 삭제할 수 없습니다.");
		}
		return "redirect:/bank";
	}

	private String ownerId(HttpSession session) {
		return (String) session.getAttribute(LOGIN_ACCOUNT_ID_KEY);
	}
}

