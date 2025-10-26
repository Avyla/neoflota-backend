package org.avyla;

import org.avyla.checklists.config.ChecklistProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(ChecklistProperties.class)
@SpringBootApplication
public class NeoFlotaApplication {
    public static void main(String[] args) {
        SpringApplication.run(NeoFlotaApplication.class, args);

    }
}
