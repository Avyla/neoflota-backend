package org.avyla.vehicles.domain.repo;


import org.avyla.vehicles.domain.model.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.LockModeType;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Vehicle v where v.vehicleId = :id")
    Optional<Vehicle> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            SELECT v FROM Vehicle v
            WHERE
                (v.soatExpirationDate IS NOT NULL AND v.soatExpirationDate <= :limitDate)
             OR (v.rtmExpirationDate  IS NOT NULL AND v.rtmExpirationDate  <= :limitDate)
            """)
    List<Vehicle> findExpiringByDate(@Param("limitDate") java.time.LocalDate limitDate);

    Optional<Vehicle> findByPlate(String plate);

    Page<Vehicle> findByActive(boolean active, Pageable pageable);

}
