package org.avyla.vehicles.domain.repo;

import org.avyla.vehicles.domain.model.VehicleDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.avyla.vehicles.infrastructure.DocumentType;

import java.util.List;
import java.util.UUID;

public interface VehicleDocumentRepository extends JpaRepository<VehicleDocument, UUID> {

    List<VehicleDocument> findByVehicle_VehicleIdAndDocTypeOrderByCreatedAtDesc(Long vehicleId, DocumentType docType);

}
