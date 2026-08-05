package grillogic.controller;

import grillogic.model.Vendor;
import grillogic.repository.VendorRepository;
import grillogic.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {

    private final VendorRepository vendorRepository;
    private final CurrentUserService currentUserService;

    @Autowired
    public VendorController(VendorRepository vendorRepository,
                            CurrentUserService currentUserService) {
        this.vendorRepository = vendorRepository;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public Vendor createVendor(@RequestBody Vendor vendor) {
        Long ownerId = currentUserService.getCurrentUser().getId();
        vendor.setOwnerId(ownerId);
        return vendorRepository.save(vendor);
    }

    @GetMapping
    public List<Vendor> getAllVendors() {
        Long ownerId = currentUserService.getCurrentUser().getId();
        return vendorRepository.findAll().stream()
                .filter(v -> v.getOwnerId().equals(ownerId))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Vendor getVendor(@PathVariable Long id) {
        return vendorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found: " + id));
    }

    @PutMapping("/{id}")
    public Vendor updateVendor(@PathVariable Long id, @RequestBody Vendor updated) {
        Vendor existing = vendorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found: " + id));

        existing.setName(updated.getName());
        existing.setContactName(updated.getContactName());
        existing.setContactEmail(updated.getContactEmail());
        existing.setContactPhone(updated.getContactPhone());
        existing.setNotes(updated.getNotes());

        return vendorRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    public void deleteVendor(@PathVariable Long id) {
        if (!vendorRepository.existsById(id)) {
            throw new RuntimeException("Vendor not found: " + id);
        }
        vendorRepository.deleteById(id);
    }
}