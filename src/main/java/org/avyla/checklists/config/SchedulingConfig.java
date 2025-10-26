package org.avyla.checklists.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling // habilita detección de @Scheduled
public class SchedulingConfig {}
