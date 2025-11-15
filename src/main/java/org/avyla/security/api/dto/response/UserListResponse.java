package org.avyla.security.api.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.avyla.vehicles.domain.enums.DocumentIdentityType;

import java.util.List;

/**
 * DTO for user list item in admin panel
 * Lightweight version for listing view
 */
@JsonPropertyOrder({
        "userId",
        "username",
        "email",
        "firstName",
        "lastName",
        "documentType",
        "documentNumber",
        "roles",
        "isEnabled"
})
public record UserListResponse(
        Long userId,
        String username,
        String email,
        String firstName,
        String lastName,
        DocumentIdentityType documentType,
        String documentNumber,
        List<String> roles,
        Boolean isEnabled
) {
}