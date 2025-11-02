package org.avyla.security.api.dto.request;

import jakarta.validation.constraints.Size;

import java.util.List;

public record AuthCreateRoleRequest
        (
                @Size(max = 3, message = "The user canot have more than 3 roles")List<String> roleListName
        )
{
}
