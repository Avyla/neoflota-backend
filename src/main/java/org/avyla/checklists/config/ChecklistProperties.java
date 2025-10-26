package org.avyla.checklists.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "checklists")
public class ChecklistProperties {
    private Generation generation = new Generation();
    private Instance instance = new Instance();

    @Data
    public static class Generation {
        private boolean enabled = true;
    }
    @Data
    public static class Instance {
        private int ttlMinutes = 60;
        private int cooldownMinutes = 15;
    }
}
