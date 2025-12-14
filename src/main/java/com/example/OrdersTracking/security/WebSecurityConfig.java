package com.example.OrdersTracking.security;

import com.example.OrdersTracking.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public BCryptPasswordEncoder bCryptPasswordEncoder;

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }

    @Bean
    public AuthenticationSuccessHandler customOAuth2LoginSuccessHandler() {
        return new CustomOAuth2LoginSuccessHandler(userRepository, bCryptPasswordEncoder);
    }

    // 1. IGNORE STATIC RESOURCES (Vital for PWA)
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/sw.js", "/images/**", "/js/**", "/css/**", "/favicon.ico");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        // 2. PROTECT ADMIN & KITCHEN
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/kitchen/**").hasAuthority("COOKER")
                        // 3. ALLOW EVERYTHING ELSE (Menu, Home, etc.)
                        .anyRequest().permitAll()
                )
                .formLogin((form) -> form
                        .loginPage("/user/login")
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/user/login")
                        .successHandler(customOAuth2LoginSuccessHandler())
                )
                .logout((logout) -> logout.logoutSuccessUrl("/").permitAll())

                // 4. HANDLE ERRORS WITHOUT LOOPS
                .exceptionHandling(e -> e
                        // Case A: User is logged in, but wrong Role (e.g. Cook tries to access Admin)
                        .accessDeniedHandler((request, response, ex) -> {
                            System.out.println("⛔ ACCESS DENIED: " + request.getRequestURI());
                            response.sendRedirect("/");
                        })

                        // Case B: User is NOT logged in (Anonymous tries to access Admin)
                        .authenticationEntryPoint((request, response, ex) -> {
                            System.out.println("🔒 UNAUTHORIZED ACCESS: " + request.getRequestURI());
                            // Force redirect to Home to stop any possible loops
                            response.sendRedirect("/");
                        })
                );

        return http.build();
    }
}