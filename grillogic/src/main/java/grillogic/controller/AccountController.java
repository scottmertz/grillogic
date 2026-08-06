package grillogic.controller;

import grillogic.controller.dto.AddManagerRequest;
import grillogic.controller.dto.ManagerResponse;
import grillogic.model.User;
import grillogic.repository.UserRepository;
import grillogic.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// Lets a primary account owner invite/remove secondary "manager" logins that
// share access to the same data (Ingredients, Recipes, Vendors, etc.) via
// CurrentUserService.getEffectiveOwnerId().
@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AccountController(UserRepository userRepository,
                             CurrentUserService currentUserService,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.passwordEncoder = passwordEncoder;
    }

    // Only a primary account owner (linkedOwnerId == null) may view/manage the team.
    // A manager account hitting these endpoints just gets rejected — there's nothing
    // sensitive in the rejection itself.
    private User requireOwner() {
        User current = currentUserService.getCurrentUser();
        if (current.getLinkedOwnerId() != null) {
            throw new RuntimeException("Only the primary account owner can manage team members.");
        }
        return current;
    }

    @GetMapping("/managers")
    public List<ManagerResponse> getManagers() {
        User owner = requireOwner();
        return userRepository.findByLinkedOwnerId(owner.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @PostMapping("/managers")
    public ManagerResponse addManager(@RequestBody AddManagerRequest request) {
        User owner = requireOwner();

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters");
        }

        User manager = new User();
        manager.setEmail(request.getEmail());
        manager.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        manager.setRole("CLIENT");
        manager.setBusinessName(owner.getBusinessName());
        manager.setLinkedOwnerId(owner.getId());
        manager.setAccountRole("MANAGER");

        userRepository.save(manager);
        return toResponse(manager);
    }

    @DeleteMapping("/managers/{id}")
    public void removeManager(@PathVariable Long id) {
        User owner = requireOwner();

        User manager = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team member not found: " + id));

        if (!owner.getId().equals(manager.getLinkedOwnerId())) {
            throw new RuntimeException("Access denied: not your team member");
        }

        userRepository.deleteById(id);
    }

    private ManagerResponse toResponse(User user) {
        ManagerResponse dto = new ManagerResponse();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setAccountRole(user.getAccountRole());
        return dto;
    }
}