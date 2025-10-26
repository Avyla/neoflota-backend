package org.avyla.checklists.config.jobs;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.avyla.checklists.domain.repo.ChecklistInstanceRepository;
import org.avyla.checklists.infrastructure.InstanceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChecklistExpireJob {


    private final ChecklistInstanceRepository instanceRepo;

    // Corre cada minuto en zona America/Bogota
    @Scheduled(cron = "0 * * * * *", zone = "America/Bogota")
    public void expireStaleInstances() {
        Instant now = Instant.now();
        int updated = instanceRepo.expireDueInstances(
                now,
                List.of(InstanceStatus.PENDING, InstanceStatus.IN_PROGRESS),
                InstanceStatus.EXPIRED
        );
        if (updated > 0) {
            log.info("ChecklistExpireJob: {} instancias expiradas a {}", updated, now);
        }
    }
}
