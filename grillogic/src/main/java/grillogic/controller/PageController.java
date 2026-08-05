package grillogic.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/ingredients")
    public String ingredientsPage() {
        return "ingredients";
    }

    @GetMapping("/vendors")
    public String vendorsPage() {
        return "vendors";
    }

    @GetMapping("/recipes")
    public String recipesPage() {
        return "recipes";
    }

    @GetMapping("/recipes/list")
    public String recipesListPage() {
        return "recipes-list";
    }

    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "dashboard";
    }

    @GetMapping("/recipes/{id}/view")
    public String recipeDetailPage() {
        return "recipe-detail";
    }

    @GetMapping("/recipes/{id}/edit")
    public String recipeEditPage() {
        return "recipe-edit";
    }

    @GetMapping("/admin")
    public String adminPage() {
        return "admin";
    }

    @GetMapping("/admin/clients/{id}")
    public String adminClientDetailPage() {
        return "admin-client-detail";
    }

    @GetMapping("/account")
    public String accountPage() {
        return "account";
    }
}