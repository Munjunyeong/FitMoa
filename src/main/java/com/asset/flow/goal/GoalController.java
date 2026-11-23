package com.asset.flow.goal;

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
@RequestMapping("/goals")
public class GoalController {
    private static final String LOGIN_ACCOUNT_ID_KEY = "LOGIN_ACCOUNT_ID";
    private final GoalService goalService;
    public GoalController(GoalService goalService) { this.goalService = goalService; }

    @GetMapping
    public String list(HttpSession session, Model model) {
        String ownerId = ownerId(session);
        if (ownerId == null) return "redirect:/login";
        List<Goal> goals = goalService.getGoals(ownerId);
        long currentTotal = goals.stream().mapToLong(Goal::getCurrentAmount).sum();
        long targetTotal = goals.stream().mapToLong(Goal::getTargetAmount).sum();
        Goal nearest = goals.stream().filter(g -> g.getDaysRemaining() >= 0)
                .min((a, b) -> a.getEndDate().compareTo(b.getEndDate())).orElse(null);
        model.addAttribute("goals", goals);
        model.addAttribute("currentTotal", currentTotal);
        model.addAttribute("targetTotal", targetTotal);
        model.addAttribute("overallProgress", targetTotal == 0 ? 0 : Math.min(100, Math.round(currentTotal * 100.0 / targetTotal)));
        model.addAttribute("nearestGoal", nearest);
        return "goals";
    }

    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        if (ownerId(session) == null) return "redirect:/login";
        model.addAttribute("goal", new Goal());
        model.addAttribute("pageTitle", "저축함 만들기");
        model.addAttribute("formAction", "/goals");
        return "goal-form";
    }

    @PostMapping
    public String create(HttpSession session, @ModelAttribute Goal goal, RedirectAttributes redirect) {
        String ownerId = ownerId(session);
        if (ownerId == null) return "redirect:/login";
        try {
            goalService.createGoal(ownerId, goal);
            redirect.addFlashAttribute("message", "저축 목표를 만들었습니다.");
        } catch (IllegalArgumentException e) { redirect.addFlashAttribute("error", e.getMessage()); return "redirect:/goals/new"; }
        return "redirect:/goals";
    }

    @GetMapping("/{goalId}/edit")
    public String editForm(@PathVariable Long goalId, HttpSession session, Model model, RedirectAttributes redirect) {
        String ownerId = ownerId(session);
        if (ownerId == null) return "redirect:/login";
        try {
            model.addAttribute("goal", goalService.getGoal(goalId, ownerId));
            model.addAttribute("pageTitle", "저축함 수정");
            model.addAttribute("formAction", "/goals/" + goalId + "/edit");
            return "goal-form";
        } catch (IllegalArgumentException e) { redirect.addFlashAttribute("error", e.getMessage()); return "redirect:/goals"; }
    }

    @PostMapping("/{goalId}/edit")
    public String update(@PathVariable Long goalId, HttpSession session, @ModelAttribute Goal goal, RedirectAttributes redirect) {
        String ownerId = ownerId(session);
        if (ownerId == null) return "redirect:/login";
        try { goalService.updateGoal(goalId, ownerId, goal); redirect.addFlashAttribute("message", "저축 목표를 수정했습니다."); }
        catch (IllegalArgumentException e) { redirect.addFlashAttribute("error", e.getMessage()); return "redirect:/goals/" + goalId + "/edit"; }
        return "redirect:/goals";
    }

    @PostMapping("/{goalId}/deposit")
    public String deposit(@PathVariable Long goalId, HttpSession session, @RequestParam long amount, RedirectAttributes redirect) {
        return changeAmount(goalId, session, amount, true, redirect);
    }
    @PostMapping("/{goalId}/withdraw")
    public String withdraw(@PathVariable Long goalId, HttpSession session, @RequestParam long amount, RedirectAttributes redirect) {
        return changeAmount(goalId, session, amount, false, redirect);
    }
    private String changeAmount(Long goalId, HttpSession session, long amount, boolean deposit, RedirectAttributes redirect) {
        String ownerId = ownerId(session);
        if (ownerId == null) return "redirect:/login";
        try {
            if (deposit) goalService.deposit(goalId, ownerId, amount); else goalService.withdraw(goalId, ownerId, amount);
            redirect.addFlashAttribute("message", deposit ? "목표 금액에 적립했습니다." : "목표 금액에서 출금했습니다.");
        } catch (IllegalArgumentException e) { redirect.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/goals";
    }

    @PostMapping("/{goalId}/delete")
    public String delete(@PathVariable Long goalId, HttpSession session, RedirectAttributes redirect) {
        String ownerId = ownerId(session);
        if (ownerId == null) return "redirect:/login";
        try { goalService.deleteGoal(goalId, ownerId); redirect.addFlashAttribute("message", "저축 목표를 삭제했습니다."); }
        catch (IllegalArgumentException e) { redirect.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/goals";
    }

    private String ownerId(HttpSession session) { return (String) session.getAttribute(LOGIN_ACCOUNT_ID_KEY); }
}
