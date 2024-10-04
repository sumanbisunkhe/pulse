package com.sb.pulse.controller;

import com.sb.pulse.dto.UserDto;
import com.sb.pulse.enums.Status;
import com.sb.pulse.model.User;
import com.sb.pulse.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;

    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        // Get the username of the logged-in user from the Principal object
        String username = principal.getName();
        Status status= Status.ACTIVE;

        // Set user status to active
        userService.setUserStatus(username,status);

        // Fetch the user details based on the username
        Optional<User> userOptional = userService.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Set user status to active
            user.setStatus(Status.ACTIVE);
            // ProfilePicture contains just the filename
            String profilePictureUrl =user.getProfilePicture();
            model.addAttribute("profilePictureUrl", profilePictureUrl);
            model.addAttribute("user", user);
            return "users/dashboard";
        }
        else {
            // Handle the case where the user is not found (though it shouldn't happen in normal login flow)
            model.addAttribute("errorMessage", "User not found.");
            return "error"; // Optionally, redirect to an error page
        }
    }

    @GetMapping("/profile-picture/{id}")
    public String profilePicture(@PathVariable("id") int id, Model model, Principal principal) {
        // Get the username of the logged-in user from the Principal object
        String username = principal.getName();

        // Fetch the user details based on the id parameter
        Optional<User> userOptional = userService.findById(id);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Check if the logged-in user is the same as the requested user
            if (user.getUsername().equals(username)) {
                // ProfilePicture contains just the filename
                String profilePictureUrl = user.getProfilePicture();
                model.addAttribute("profilePictureUrl", profilePictureUrl);
                model.addAttribute("user", user);
                return "users/userViewProfile";
            } else {
                model.addAttribute("errorMessage", "Access denied: You can only view your own profile picture.");
                return "error"; // Optionally, handle unauthorized access
            }
        } else {
            // Handle the case where the user is not found
            model.addAttribute("errorMessage", "User not found.");
            return "error"; // Optionally, redirect to an error page
        }
    }

    // log-out
    @GetMapping("/log-out")
    public String logout(Model model) {

        return "users/login";

    }



    // Show form to edit an existing user
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") int id, Model model) {
        UserDto userDto = userService.getUserById(id);
        model.addAttribute("user", userDto);
        return "users/userEdit";
    }

    // Update user by id
    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable("id") int id, @Valid @ModelAttribute("userDto") UserDto userDto, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            userDto.setId(id);
            return "users/userEdit";

        }
        try {
            userService.updateUser(id, userDto);
            redirectAttributes.addFlashAttribute("successMessage", "Information updated successfully.");

            return "redirect:/users/dashboard";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "users/userEdit";
        }
    }
    // View user details by id
    @GetMapping("id/{id}")
    public String viewUser(@PathVariable("id") int id, Model model) {
        UserDto userDto = userService.getUserById(id);
        model.addAttribute("user", userDto);
        return "users/userViewOne";
    }

    // Delete user by id
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") int id) {
        userService.deleteUser(id);
        return "redirect:/users";
    }


}
