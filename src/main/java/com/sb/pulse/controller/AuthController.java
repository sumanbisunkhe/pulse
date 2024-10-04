package com.sb.pulse.controller;

import com.sb.pulse.dto.UserDto;
import com.sb.pulse.service.UserService;
import com.sb.pulse.utils.CustomAuthenticationSuccessHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;


    @Autowired
    public AuthController(UserService userService, @Lazy AuthenticationManager authenticationManager, AuthenticationSuccessHandler successHandler, CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
    }

    // Show Login form
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("user", new UserDto());
        return "users/login"; // Return the login template
    }

    @PostMapping("/login")
    public String authenticateUser(@RequestParam String identifier, @RequestParam String password,
                                   HttpServletRequest request, HttpServletResponse response,
                                   RedirectAttributes redirectAttributes) throws ServletException, IOException {
        try {
            // Create an authentication token
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(identifier, password);

            // Authenticate the user using the authentication manager
            Authentication authResult = authenticationManager.authenticate(authRequest);

            // Check if authentication was successful
            if (authResult.isAuthenticated()) {
                // Set authentication to the SecurityContextHolder
                SecurityContextHolder.getContext().setAuthentication(authResult);

                // Manually set the authentication into session
                request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

                // Use the custom success handler for redirection
                customAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authResult);

                return null;  // Return null since the success handler will handle the redirection
            }
        } catch (AuthenticationException e) {
            // If authentication fails, redirect to login error page
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid username, email, or password.");
            return "redirect:/auth/login";
        }

        // Fallback in case of error
        redirectAttributes.addFlashAttribute("errorMessage", "Authentication failed.");
        return "redirect:/auth/login";
    }


    // Show Create new User form
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new UserDto());
        return "users/create";
    }

    // Save new User
    @PostMapping("/create")
    public String saveUser(@Valid @ModelAttribute("user") UserDto userDto, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            // Return the userDto back to the form to display the errors
            model.addAttribute("user", userDto); // Keep the submitted data
            return "users/create"; // Return to the form page with errors
        }

        try {
            UserDto createdUser = userService.createUser(userDto);
            redirectAttributes.addFlashAttribute("successMessage", "Account created successfully! Login");
            return "redirect:/auth/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("user", userDto); // Keep the submitted data
            return "users/create"; // Return to the form page with error message
        }
    }

    // Get profile picture by filename
    @GetMapping("/pp/{filename}")
    public ResponseEntity<Resource> getProfilePicture(@PathVariable String filename) {
            try {
                Path filePath = Paths.get("D:/JAVA/pulse/uploads").resolve(filename).normalize();
                Resource resource = new UrlResource(filePath.toUri());

                if (resource.exists()) {
                    // Determine the file's content type
                    String contentType = Files.probeContentType(filePath);

                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType)) // dynamic media type
                            .body(resource);
                } else {
                    return ResponseEntity.notFound().build();
                }
            } catch (IOException e) {
                return ResponseEntity.badRequest().build();
            }

    }



}
