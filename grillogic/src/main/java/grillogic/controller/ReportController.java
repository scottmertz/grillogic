package grillogic.controller;

import grillogic.model.Recipe;
import grillogic.repository.RecipeRepository;
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

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final RecipeRepository recipeRepository;
    private final PdfReportService pdfReportService;
    private final CurrentUserService currentUserService;

    @Autowired
    public ReportController(RecipeRepository recipeRepository,
                            PdfReportService pdfReportService,
                            CurrentUserService currentUserService) {
        this.recipeRepository = recipeRepository;
        this.pdfReportService = pdfReportService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/full-audit")
    public ResponseEntity<byte[]> getFullAuditReport() {
        Long ownerId = currentUserService.getCurrentUser().getId();

        List<Recipe> recipes = recipeRepository.findAll().stream()
                .filter(r -> r.getOwnerId().equals(ownerId))
                .collect(Collectors.toList());

        byte[] pdfBytes = pdfReportService.generateFullAuditReport(recipes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "GRILLOGIC_Full_Audit_Report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}