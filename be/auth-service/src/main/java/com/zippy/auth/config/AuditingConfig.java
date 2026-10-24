package com.zippy.auth.config;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import java.time.Clock;
@Configuration @EnableJpaAuditing
public class AuditingConfig {
    @Bean public Clock clock() { return Clock.systemUTC(); }
}
