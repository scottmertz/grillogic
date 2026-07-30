package grillogic.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Thymeleaf looks for templates/login.html
    }

    @GetMapping("/ingredients")
    public String ingredientsPage() {
        return "ingredients";
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
}