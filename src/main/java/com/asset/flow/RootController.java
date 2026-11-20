package com.asset.flow;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.asset.flow.account.Account;
import com.asset.flow.account.AccountService;

import jakarta.servlet.http.HttpSession;

@Controller
public class RootController {
    private static final String LOGIN_ACCOUNT_ID_KEY = "LOGIN_ACCOUNT_ID";

    private final AccountService accountService;

    public RootController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/")
    String index() {
        return "index";
    }

    @GetMapping({"/register", "/signup"})
    public String registerForm() {
        return "signup"; // templates/signup.html
    }

    @PostMapping({"/register", "/signup", "/accounts/signup"})
    public String register(
            Account item,
            @RequestParam(name = "username", required = false) String username
    ) {
        if ((item.getId() == null || item.getId().isBlank()) && username != null) {
            item.setId(username);
        }
        accountService.register(item);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(HttpSession session) {
        if (session.getAttribute(LOGIN_ACCOUNT_ID_KEY) == null) {
            return "login"; // templates/login.html
        }
        return "redirect:/";
    }

    @PostMapping("/login")
    public String login(Account item, HttpSession session) {
        boolean result = accountService.login(item);
        if (result) {
            session.setAttribute(LOGIN_ACCOUNT_ID_KEY, item.getId());
            return "redirect:/";
        }
        return "redirect:/login?error=false";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}