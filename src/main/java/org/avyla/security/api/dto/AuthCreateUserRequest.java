package org.avyla.security.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.avyla.vehicles.infrastructure.DocumentIdentityType;

public record AuthCreateUserRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String email,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull DocumentIdentityType documentIdentityType,
        @NotBlank String documentNumber,
        String phoneNumber,
        @Valid AuthCreateRoleRequest roleRequest
)
{
}
