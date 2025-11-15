package org.avyla.security.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.avyla.vehicles.domain.enums.DocumentIdentityType;

/**
 * DTO for updating user information by ADMIN
 * All fields are optional - only provided fields will be updated
 */
public record UpdateUserRequest(

        @Email(message = "Invalid email format")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        String email,

        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        DocumentIdentityType documentIdentityType,

        @Size(max = 50, message = "Document number must not exceed 50 characters")
        String documentNumber,

        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        String phoneNumber,

        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        Boolean isEnable,

        @Valid
        AuthCreateRoleRequest roleRequest
) {
    /**
     * Check if any field is provided for update
     */
    public boolean hasAnyFieldToUpdate() {
        return email != null
                || firstName != null
                || lastName != null
                || documentIdentityType != null
                || documentNumber != null
                || phoneNumber != null
                || username != null
                || isEnable != null
                || roleRequest != null;
    }
}