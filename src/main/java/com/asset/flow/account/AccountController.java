package com.asset.flow.account;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/account")
public class AccountController {

	private static final String LOGIN_ACCOUNT_ID_KEY = "LOGIN_ACCOUNT_ID";

	private final AccountService accountService;

	public AccountController(AccountService accountService) {
		this.accountService = accountService;
	}

	@GetMapping("/profile")
	public String profile(HttpSession session, Model model) {
		String id = (String) session.getAttribute(LOGIN_ACCOUNT_ID_KEY);
		if (id == null) {
			return "redirect:/login";
		}
		Account account = accountService.getAccount(id);
		model.addAttribute("account", account);
		return "account/profile";
	}

	@GetMapping("/profile/edit")
	public String editForm(HttpSession session, Model model) {
		String id = (String) session.getAttribute(LOGIN_ACCOUNT_ID_KEY);
		if (id == null) {
			return "redirect:/login";
		}
		Account account = accountService.getAccount(id);
		model.addAttribute("account", account);
		return "account/edit";
	}

	@PostMapping("/profile/edit")
	public String update(
			HttpSession session,
			@ModelAttribute Account accountForm,
			RedirectAttributes redirectAttributes
	) {
		String id = (String) session.getAttribute(LOGIN_ACCOUNT_ID_KEY);
		if (id == null) {
			return "redirect:/login";
		}
		try {
			accountService.updateAccount(id, accountForm);
			redirectAttributes.addFlashAttribute("message", "계정 정보가 수정되었습니다.");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/account/profile/edit";
		}
		return "redirect:/account/profile";
	}

	@PostMapping("/profile/delete")
	public String delete(HttpSession session, RedirectAttributes redirectAttributes) {
		String id = (String) session.getAttribute(LOGIN_ACCOUNT_ID_KEY);
		if (id == null) {
			return "redirect:/login";
		}
		accountService.deleteAccount(id);
		session.invalidate();
		redirectAttributes.addFlashAttribute("message", "계정이 삭제되었습니다.");
		return "redirect:/login";
	}
}
