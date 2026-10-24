package com.zippy.auth.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;
@ConfigurationProperties(prefix = "auth.google")
public record GoogleProperties(List<String> clientIds) {}
