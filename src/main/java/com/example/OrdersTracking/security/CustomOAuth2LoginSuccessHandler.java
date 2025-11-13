package com.example.OrdersTracking.security;

import com.example.OrdersTracking.models.User;
import com.example.OrdersTracking.repositories.UserRepository;
import com.example.OrdersTracking.services.SseService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

public class CustomOAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private SseService sseService;


    public CustomOAuth2LoginSuccessHandler(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        // Set the default URL to redirect to after successful login
        setDefaultTargetUrl("/");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");
        User user = userRepository.getUserByEmail(email);

        if (user == null) {
            // New user, register them
            user = createNewUser(oauthUser);
        }

        // Log the user in using our application's standard security context
        MyUserDetails userDetails = new MyUserDetails(user);
        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        newAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        // Redirect to the default target URL
        super.onAuthenticationSuccess(request, response, authentication);
    }

    private User createNewUser(OAuth2User oauthUser) {
        User newUser = new User();
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        newUser.setEmail(email);
        newUser.setEnabled(true);
        newUser.setRole("User");

        // Create a secure, random password as it's required by our entity but won't be used for login
        String randomPassword = UUID.randomUUID().toString();
        newUser.setPassword(passwordEncoder.encode(randomPassword));

        // Generate a unique username
        String baseUsername = (name != null ? name.replaceAll("\\s+", "") : "user");
        String finalUsername = baseUsername;
        int counter = 1;
        while (userRepository.getUserByUsername(finalUsername) != null) {
            finalUsername = baseUsername + (new Random().nextInt(9000) + 1000); // e.g., "johnDoe1234"
        }
        newUser.setUsername(finalUsername);

        User savedUser = userRepository.save(newUser);
        sseService.sendNewUserEvent(savedUser);

        return savedUser;
    }
}