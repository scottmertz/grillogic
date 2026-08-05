package grillogic.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import grillogic.controller.dto.ReportRow;
import grillogic.model.Recipe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PdfReportService {

    private final TemplateEngine templateEngine;
    private final CostingService costingService;

    @Autowired
    public PdfReportService(TemplateEngine templateEngine, CostingService costingService) {
        this.templateEngine = templateEngine;
        this.costingService = costingService;
    }

    public byte[] generateFullAuditReport(List<Recipe> recipes, String tier) {
        List<ReportRow> rows = recipes.stream()
                .map(this::toReportRow)
                .collect(Collectors.toList());

        long criticalCount = rows.stream().filter(r -> "status-bad".equals(r.getStatusClass())).count();
        long highCount = rows.stream().filter(r -> "status-warn".equals(r.getStatusClass())).count();
        long solidCount = rows.stream().filter(r -> "status-good".equals(r.getStatusClass())).count();

        // Tier gating: Recovery Plan requires STANDARD or PREMIUM.
        // 30-Day Roadmap requires PREMIUM only.
        boolean showRecoveryPlan = "STANDARD".equals(tier) || "PREMIUM".equals(tier);
        boolean showRoadmap = "PREMIUM".equals(tier);

        Context context = new Context();
        context.setVariable("recipes", rows);
        context.setVariable("reportDate",
                LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        context.setVariable("totalDishes", rows.size());
        context.setVariable("criticalCount", criticalCount);
        context.setVariable("highCount", highCount);
        context.setVariable("solidCount", solidCount);
        context.setVariable("actionRows", rows.stream()
                .filter(r -> r.getSuggestedPriceDisplay() != null)
                .collect(Collectors.toList()));
        context.setVariable("showRecoveryPlan", showRecoveryPlan);
        context.setVariable("showRoadmap", showRoadmap);

        String html = templateEngine.process("audit-report", context);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, "http://localhost:8080/");
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    public byte[] generatePrepCard(Recipe recipe) {
        List<Map<String, Object>> lines = recipe.getIngredients().stream()
                .map(line -> {
                    Map<String, Object> lineMap = new HashMap<>();
                    if (line.getIngredient() != null) {
                        lineMap.put("name", line.getIngredient().getName());
                    } else {
                        lineMap.put("name", line.getSubRecipe().getName());
                    }
                    lineMap.put("amount", line.getAmount());
                    lineMap.put("unit", line.getAmountUnit());
                    return lineMap;
                })
                .collect(Collectors.toList());

        Context context = new Context();
        context.setVariable("recipeName", recipe.getName());
        context.setVariable("servings", recipe.getServings());
        context.setVariable("lineCount", lines.size());
        context.setVariable("lines", lines);
        context.setVariable("instructions",
                recipe.getInstructions() != null ? recipe.getInstructions() : "No instructions provided.");

        String html = templateEngine.process("prep-card", context);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, "http://localhost:8080/");
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    private ReportRow toReportRow(Recipe recipe) {
        ReportRow row = new ReportRow();
        row.setName(recipe.getName());

        Double menuPrice = recipe.getMenuPrice();
        row.setMenuPriceDisplay(menuPrice != null ? String.format("$%.2f", menuPrice) : "—");

        double costPerServing = costingService.costPerServing(recipe);
        row.setCostPerServingDisplay(String.format("$%.2f", costPerServing));

        Double foodCostPct = costingService.foodCostPercent(recipe);
        if (foodCostPct == null) {
            row.setFoodCostPctDisplay("N/A");
            row.setStatusLabel("N/A");
            row.setStatusClass("");
            row.setRowClass("");
            row.setSuggestedPriceDisplay(null);
        } else {
            double pct = foodCostPct * 100;
            row.setFoodCostPctDisplay(String.format("%.1f%%", pct));

            if (pct >= 35) {
                row.setStatusLabel("CRITICAL");
                row.setStatusClass("status-bad");
                row.setRowClass("row-bad");
            } else if (pct >= 30) {
                row.setStatusLabel("HIGH");
                row.setStatusClass("status-warn");
                row.setRowClass("row-warn");
            } else {
                row.setStatusLabel("SOLID");
                row.setStatusClass("status-good");
                row.setRowClass("row-good");
            }

            if (pct >= 30) {
                double suggestedPrice = costPerServing / 0.30;
                row.setSuggestedPriceDisplay(String.format("$%.2f", suggestedPrice));
            } else {
                row.setSuggestedPriceDisplay(null);
            }
        }

        return row;
    }
}