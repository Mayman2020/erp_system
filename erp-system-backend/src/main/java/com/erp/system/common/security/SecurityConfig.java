package com.erp.system.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] ADMIN_ROLES = {"ADMIN"};
    private static final String[] ACCOUNTING_READ_ROLES = {
            "ADMIN", "ACCOUNTANT", "ACCOUNTANT_STANDARD", "TREASURY_OPERATOR", "REPORT_VIEWER", "MANAGER"
    };
    private static final String[] ACCOUNTING_WRITE_ROLES = {
            "ADMIN", "ACCOUNTANT", "ACCOUNTANT_STANDARD", "TREASURY_OPERATOR", "MANAGER"
    };
    private static final String[] INVENTORY_ROLES = {
            "ADMIN", "MANAGER", "INVENTORY", "ACCOUNTANT", "ACCOUNTANT_STANDARD"
    };
    private static final String[] SALES_ROLES = {
            "ADMIN", "MANAGER", "SALES", "ACCOUNTANT", "ACCOUNTANT_STANDARD"
    };
    private static final String[] PURCHASE_ROLES = {
            "ADMIN", "MANAGER", "PURCHASE", "ACCOUNTANT", "ACCOUNTANT_STANDARD"
    };
    private static final String[] HR_ROLES = {
            "ADMIN", "MANAGER", "HR", "ACCOUNTANT", "ACCOUNTANT_STANDARD"
    };
    private static final String[] CRM_ROLES = {
            "ADMIN", "MANAGER", "SALES", "ACCOUNTANT", "ACCOUNTANT_STANDARD"
    };
    private static final String[] PROJECT_ROLES = {
            "ADMIN", "MANAGER", "ACCOUNTANT", "ACCOUNTANT_STANDARD"
    };
    private static final String[] MANUFACTURING_ROLES = {
            "ADMIN", "MANAGER", "INVENTORY", "ACCOUNTANT_STANDARD"
    };
    private static final String[] ERP_READ_ROLES = {
            "ADMIN", "MANAGER", "REPORT_VIEWER", "ACCOUNTANT", "ACCOUNTANT_STANDARD"
    };
    private static final String[] ERP_WRITE_ROLES = {
            "ADMIN", "MANAGER", "ACCOUNTANT", "ACCOUNTANT_STANDARD"
    };

    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final MenuActionAuthorizationFilter menuActionAuthorizationFilter;
    private final AuthenticationEntryPoint apiAuthenticationEntryPoint;
    private final AccessDeniedHandler apiAccessDeniedHandler;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(apiAuthenticationEntryPoint)
                        .accessDeniedHandler(apiAccessDeniedHandler)
                )
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/health").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/auth/login", "/auth/login/roles", "/auth/refresh",
                                "/auth/password/otp/send", "/auth/password/otp/reset").permitAll()
                        .requestMatchers("/auth/register").hasRole("ADMIN")
                        .requestMatchers("/profile/**").authenticated()
                        .requestMatchers("/ui/**").authenticated()
                        .requestMatchers("/lookups/**").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/accounting/**").hasAnyRole(ACCOUNTING_READ_ROLES)
                        .requestMatchers("/accounting/**").hasAnyRole(ACCOUNTING_WRITE_ROLES)

                        .requestMatchers(HttpMethod.GET, "/inventory/**").hasAnyRole(INVENTORY_ROLES)
                        .requestMatchers("/inventory/**").hasAnyRole(INVENTORY_ROLES)

                        .requestMatchers(HttpMethod.GET, "/sales/**").hasAnyRole(SALES_ROLES)
                        .requestMatchers("/sales/**").hasAnyRole(SALES_ROLES)

                        .requestMatchers(HttpMethod.GET, "/purchases/**").hasAnyRole(PURCHASE_ROLES)
                        .requestMatchers("/purchases/**").hasAnyRole(PURCHASE_ROLES)

                        .requestMatchers(HttpMethod.GET, "/hr/**").hasAnyRole(HR_ROLES)
                        .requestMatchers("/hr/**").hasAnyRole(HR_ROLES)

                        .requestMatchers(HttpMethod.GET, "/crm/**").hasAnyRole(CRM_ROLES)
                        .requestMatchers("/crm/**").hasAnyRole(CRM_ROLES)

                        .requestMatchers(HttpMethod.GET, "/projects/**").hasAnyRole(PROJECT_ROLES)
                        .requestMatchers("/projects/**").hasAnyRole(PROJECT_ROLES)

                        .requestMatchers(HttpMethod.GET, "/manufacturing/**").hasAnyRole(MANUFACTURING_ROLES)
                        .requestMatchers("/manufacturing/**").hasAnyRole(MANUFACTURING_ROLES)

                        .requestMatchers(HttpMethod.GET, "/erp/reports/**").hasAnyRole(ERP_READ_ROLES)
                        .requestMatchers(HttpMethod.GET, "/erp/**").hasAnyRole(ERP_READ_ROLES)
                        .requestMatchers("/erp/**").hasAnyRole(ERP_WRITE_ROLES)

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(menuActionAuthorizationFilter, JwtAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
