package org.avyla.vehicles.api.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record DocumentUploadResponse(UUID id, String filename)
{

}
