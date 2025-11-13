package com.example.OrdersTracking.security;

import com.example.OrdersTracking.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }

    @Autowired
    public BCryptPasswordEncoder bCryptPasswordEncoder;

    // --- INJECT THE USER REPO ---
    @Autowired
    private UserRepository userRepository;

    // --- CREATE A BEAN FOR THE SUCCESS HANDLER ---
    @Bean
    public AuthenticationSuccessHandler customOAuth2LoginSuccessHandler() {
        return new CustomOAuth2LoginSuccessHandler(userRepository, bCryptPasswordEncoder);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/user/login")
                        .permitAll()
                )
                // --- ADD THIS OAUTH2 CONFIGURATION ---
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/user/login") // Use the same login page
                        .successHandler(customOAuth2LoginSuccessHandler()) // Use our custom handler
                )
                // ---
                .logout((logout) -> logout.logoutSuccessUrl("/").permitAll());

        return http.build();
    }
}