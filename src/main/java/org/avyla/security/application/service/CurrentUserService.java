package org.avyla.security.application.service;

import lombok.RequiredArgsConstructor;
import org.avyla.security.domain.repo.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService
{

    private final UserRepository userRepository;

    public String getCurrentUserName()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Authentication required");
        }
        return authentication.getName();
    }

    public Long getCurrentUserId()
    {
        String username = getCurrentUserName();
        return userRepository.findUserIdByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
    }

}
