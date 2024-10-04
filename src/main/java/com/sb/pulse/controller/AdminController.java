package com.sb.pulse.controller;

import com.sb.pulse.dto.UserDto;
import com.sb.pulse.enums.Status;
import com.sb.pulse.model.User;
import com.sb.pulse.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private  final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
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
            // Assuming profilePicture contains just the filename
            String profilePictureUrl =user.getProfilePicture();
            model.addAttribute("profilePictureUrl", profilePictureUrl);
            model.addAttribute("user", user);
            return "admin/dashboard";
        }
        else {
            // Handle the case where the user is not found (though it shouldn't happen in normal login flow)
            model.addAttribute("errorMessage", "User not found.");
            return "error"; // Optionally, redirect to an error page
        }
    }

    // View profile picture by id
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
                return "admin/adminViewProfile";
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

    // Show form to edit.html an existing user
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") int id, Model model) {
        UserDto userDto = userService.getUserById(id);
        model.addAttribute("user", userDto);
        return "admin/adminEdit";
    }
    // Update user by id
    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable("id") int id, @Valid @ModelAttribute("userDto") UserDto userDto, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            userDto.setId(id);
            return "admin/adminEdit";
        }
        try {
            userService.updateUser(id, userDto);
            redirectAttributes.addFlashAttribute("successMessage", "Information updated successfully.");

            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/adminEdit";
        }
    }

    // View admin details by id
    @GetMapping("id/{id}")
    public String viewUser(@PathVariable("id") int id, Model model) {
        UserDto userDto = userService.getUserById(id);
        model.addAttribute("user", userDto);
        return "admin/adminViewOne";
    }


    // Display list of users
    @GetMapping("/list")
    public String listUsers(Model model, Principal principal) {
        String username = principal.getName();

        // Fetch the logged-in user details based on the username
        Optional<User> userOptional = userService.findByUsername(username);

        if (userOptional.isPresent()) {
            User adminUser = userOptional.get();

            // Fetch the list of all users
            List<User> users = userService.findAllUsers(); // Assuming findAllUsers() fetches the list of all users

            // Remove the logged-in admin from the list of users
            users.removeIf(user -> user.getUsername().equals(adminUser.getUsername()));

            // ProfilePicture contains just the filename
            String profilePictureUrl = adminUser.getProfilePicture();
            model.addAttribute("profilePictureUrl", profilePictureUrl);
            model.addAttribute("user", adminUser);
            model.addAttribute("users", users); // Add the filtered users list to the model
            return "admin/list";
        } else {
            // Handle the case where the user is not found (though it shouldn't happen in normal login flow)
            model.addAttribute("errorMessage", "User not found.");
            return "error"; // Optionally, redirect to an error page
        }
    }


    // Delete user by id
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") int id) {
        userService.deleteUser(id);
        return "redirect:/admin/list";
    }

    // View users by Admin
    @GetMapping("/view/{id}")
    public String viewUser(@PathVariable("id") int id, Model model, Principal principal) {
        String username = principal.getName();

        // Fetch the logged-in admin details based on the username
        Optional<User> adminOptional = userService.findByUsername(username);

        // Fetch the user being viewed based on the provided id
        Optional<User> userOptional = userService.findById(id);

        if (adminOptional.isPresent() && userOptional.isPresent()) {
            User adminUser = adminOptional.get();
            User viewedUser = userOptional.get();

            // Admin's profile picture
            String adminProfilePictureUrl = adminUser.getProfilePicture();
            model.addAttribute("profilePictureUrl", adminProfilePictureUrl); // Admin's profile picture

            // User's profile picture
            String userProfilePictureUrl = viewedUser.getProfilePicture();
            model.addAttribute("userProfilePictureUrl", userProfilePictureUrl); // User's profile picture

            // Set the admin and user being viewed
            model.addAttribute("admin", adminUser);  // Admin (Viewer)
            model.addAttribute("user", viewedUser);  // User being viewed

            return "admin/viewUsersInfo";
        } else {
            // Handle the case where the user is not found
            model.addAttribute("errorMessage", "User or Admin not found.");
            return "error"; // Optionally, redirect to an error page
        }
    }






}
