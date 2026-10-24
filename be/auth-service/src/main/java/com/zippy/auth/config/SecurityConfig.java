package com.zippy.auth.config;
import com.zippy.auth.security.*;
import com.zippy.auth.filter.JwtAuthenticationFilter;
import com.zippy.auth.constant.UrlConstants;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import java.util.List;
@Configuration @EnableMethodSecurity @EnableConfigurationProperties(AuthProperties.class)
public class SecurityConfig {
    @Bean public PasswordEncoder passwordEncoder() { return new CompatiblePasswordEncoder(); }
    @Bean public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwt,
            CustomUserDetailsService users, RestAuthenticationEntryPoint entry, RestAccessDeniedHandler denied, TokenBlacklist blacklist,
            @Qualifier("corsConfigurationSource") CorsConfigurationSource cors) throws Exception {
        return http.csrf(c -> c.disable()).cors(c -> c.configurationSource(cors))
            .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .requestCache(c -> c.disable()).formLogin(c -> c.disable()).httpBasic(c -> c.disable()).logout(c -> c.disable())
            .exceptionHandling(c -> c.authenticationEntryPoint(entry).accessDeniedHandler(denied))
            .authorizeHttpRequests(c -> c
                .requestMatchers(HttpMethod.POST, UrlConstants.REGISTER, UrlConstants.LOGIN,
                    UrlConstants.REGISTER_VERIFY_OTP, UrlConstants.REGISTER_RESEND_OTP,
                    UrlConstants.FORGOT_PASSWORD, UrlConstants.FORGOT_PASSWORD_VERIFY_OTP,
                    UrlConstants.FORGOT_PASSWORD_RESEND_OTP, UrlConstants.RESET_PASSWORD,
                    UrlConstants.GOOGLE, UrlConstants.REFRESH).permitAll()
                .requestMatchers(HttpMethod.GET, UrlConstants.HEALTH, UrlConstants.LIVENESS,
                    UrlConstants.READINESS, UrlConstants.SWAGGER_UI,
                    UrlConstants.SWAGGER_UI_RESOURCES, UrlConstants.API_DOCS,
                    UrlConstants.API_DOCS_RESOURCES).permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(new JwtAuthenticationFilter(jwt, users, entry, blacklist), UsernamePasswordAuthenticationFilter.class)
            .build();
    }
    @Bean public CorsConfigurationSource corsConfigurationSource(AuthProperties properties) {
        var config = new CorsConfiguration();
        List<String> origins = properties.corsAllowedOrigins() == null ? List.of() : properties.corsAllowedOrigins();
        if (origins.stream().anyMatch(origin -> origin.contains("*"))) throw new IllegalStateException("CORS origins must be explicit");
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Auth-Flow"));
        config.setExposedHeaders(List.of("X-Auth-Flow"));
        config.setAllowCredentials(false);
        var source = new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration(UrlConstants.ALL_PATHS, config);
        return source;
    }
}
