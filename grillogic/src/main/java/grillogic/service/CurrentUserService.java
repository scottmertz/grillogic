package grillogic.service;

import grillogic.model.User;
import grillogic.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    @Autowired
    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + email));
    }

    // Returns whichever User id owns the data this person should see — their own id
    // if they're a primary account owner, or their linked owner's id if they're a
    // secondary manager login. This is what every controller should use for
    // ownerId scoping, instead of getCurrentUser().getId() directly.
    public Long getEffectiveOwnerId() {
        User current = getCurrentUser();
        return current.getLinkedOwnerId() != null ? current.getLinkedOwnerId() : current.getId();
    }
}