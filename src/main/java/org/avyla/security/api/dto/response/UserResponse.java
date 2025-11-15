package org.avyla.security.api.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.avyla.vehicles.domain.enums.DocumentIdentityType;

import java.util.List;

/**
 * Response DTO for user information
 * Contains user profile data without sensitive information (no password)
 */
@JsonPropertyOrder({
        "userId",
        "username",
        "email",
        "firstName",
        "lastName",
        "documentType",
        "documentNumber",
        "phoneNumber",
        "roles",
        "isEnabled"
})
public record UserResponse(
        Long userId,
        String username,
        String email,
        String firstName,
        String lastName,
        DocumentIdentityType documentType,
        String documentNumber,
        String phoneNumber,
        List<String> roles, // Lista de nombres de roles (ej: ["DRIVER", "MECHANIC"])
        Boolean isEnabled
) {
}