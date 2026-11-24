package com.asset.flow;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.dao.DataIntegrityViolationException;

import com.asset.flow.account.Account;
import com.asset.flow.account.AccountService;

import jakarta.servlet.http.HttpServletRequest;
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
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "passwordConfirm", required = false) String passwordConfirm,
            RedirectAttributes redirect
    ) {
        if ((item.getId() == null || item.getId().isBlank()) && username != null) {
            item.setId(username);
        }
        if (item.getPassword() == null || !item.getPassword().equals(passwordConfirm)) {
            redirect.addFlashAttribute("error", "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            return "redirect:/signup";
        }
        try {
            accountService.register(item);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/signup";
        } catch (DataIntegrityViolationException e) {
            redirect.addFlashAttribute("error", "이미 사용 중인 아이디이거나 입력한 정보가 데이터베이스 조건과 맞지 않습니다.");
            return "redirect:/signup";
        }
    }

    @GetMapping("/login")
    public String login(HttpSession session) {
        if (session.getAttribute(LOGIN_ACCOUNT_ID_KEY) == null) {
            return "login"; // templates/login.html
        }
        return "redirect:/";
    }

    @PostMapping("/login")
    public String login(Account item, HttpServletRequest request) {
        boolean result = accountService.login(item);
        if (result) {
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            request.getSession(true).setAttribute(LOGIN_ACCOUNT_ID_KEY, item.getId());
            return "redirect:/";
        }
        return "redirect:/login?error";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
