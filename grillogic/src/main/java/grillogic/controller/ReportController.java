package grillogic.controller;

import grillogic.model.Recipe;
import grillogic.model.User;
import grillogic.repository.RecipeRepository;
import grillogic.repository.UserRepository;
import grillogic.service.CurrentUserService;
import grillogic.service.PdfReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final RecipeRepository recipeRepository;
    private final PdfReportService pdfReportService;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    @Autowired
    public ReportController(RecipeRepository recipeRepository,
                            PdfReportService pdfReportService,
                            CurrentUserService currentUserService,
                            UserRepository userRepository) {
        this.recipeRepository = recipeRepository;
        this.pdfReportService = pdfReportService;
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
    }

    private void requireAdmin(User currentUser) {
        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new RuntimeException("Access denied: admin only");
        }
    }

    @GetMapping("/full-audit")
    public ResponseEntity<byte[]> getFullAuditReport() {
        User currentUser = currentUserService.getCurrentUser();
        Long ownerId = currentUserService.getEffectiveOwnerId();

        // Tier lives on the account OWNER's User record, not necessarily the
        // person currently logged in (a manager doesn't have their own tier).
        User billingOwner = ownerId.equals(currentUser.getId())
                ? currentUser
                : userRepository.findById(ownerId).orElse(currentUser);
        String tier = billingOwner.getTier();

        List<Recipe> recipes = recipeRepository.findAll().stream()
                .filter(r -> r.getOwnerId().equals(ownerId))
                .collect(Collectors.toList());

        byte[] pdfBytes = pdfReportService.generateFullAuditReport(recipes, tier);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "GRILLOGIC_Full_Audit_Report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/full-audit/{clientId}")
    public ResponseEntity<byte[]> getFullAuditReportForClient(@PathVariable Long clientId) {
        User admin = currentUserService.getCurrentUser();
        requireAdmin(admin);

        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found: " + clientId));

        List<Recipe> recipes = recipeRepository.findAll().stream()
                .filter(r -> r.getOwnerId().equals(clientId))
                .collect(Collectors.toList());

        byte[] pdfBytes = pdfReportService.generateFullAuditReport(recipes, client.getTier());

        String safeName = client.getBusinessName() != null
                ? client.getBusinessName().replaceAll("\\s+", "_")
                : "Client_" + clientId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "GRILLOGIC_Full_Audit_Report_" + safeName + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/prep-card/{recipeId}")
    public ResponseEntity<byte[]> getPrepCard(@PathVariable Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found: " + recipeId));

        byte[] pdfBytes = pdfReportService.generatePrepCard(recipe);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "GRILLOGIC_PrepCard_" + recipe.getName().replaceAll("\\s+", "_") + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}