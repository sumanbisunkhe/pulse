package com.sb.pulse.service.impl;

import com.sb.pulse.dto.UserDto;
import com.sb.pulse.enums.Status;
import com.sb.pulse.exceptions.EmailAlreadyExistsException;
import com.sb.pulse.exceptions.ResourceNotFoundException;
import com.sb.pulse.exceptions.UsernameAlreadyExistsException;
import com.sb.pulse.model.User;
import com.sb.pulse.repo.UserRepo;
import com.sb.pulse.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service

public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;

    @Autowired
    @Lazy
    public UserServiceImpl(PasswordEncoder passwordEncoder, UserRepo userRepo) {
        this.passwordEncoder = passwordEncoder;
        this.userRepo = userRepo;
    }

    // Create User
    @Override
    public UserDto createUser(UserDto userDto) {
        // Check if user with the same username and email already exists
        if(userRepo.existsByUsername(userDto.getUsername())){
            throw new UsernameAlreadyExistsException("Username '"+userDto.getUsername()+"' already exists");
        }

        if (userRepo.existsByEmail(userDto.getEmail())){
            throw new EmailAlreadyExistsException("Email '"+userDto.getEmail()+"' already exists");
        }

        // Convert to Entity
        User user = ConvertToEntity(userDto);

        // Handle profile picture upload
        MultipartFile profilePictureFile = userDto.getProfilePicture();
        if (profilePictureFile != null && !profilePictureFile.isEmpty()) {
            String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(profilePictureFile.getOriginalFilename()));
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));

            // Generate a unique file name using UUID
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            Path uploadPath = Paths.get("D:/JAVA/pulse/uploads");

            try {
                Files.createDirectories(uploadPath);
                Path filePath = uploadPath.resolve(uniqueFileName);
                Files.copy(profilePictureFile.getInputStream(), filePath);

                // Save the unique file name to the entity (as a string)
                user.setProfilePicture(uniqueFileName);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store file " + uniqueFileName, e);
            }
        }

        // Save User
        User savedUser = userRepo.save(user);

        // Convert to Dto
        return ConvertToDto(savedUser);
    }


    @Override
    public void updateUser(int id, UserDto userDto) {
        // Find the user by id
        User existingUser = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if the username is already taken by another user
        Optional<User> userWithSameUsername = userRepo.findByUsername(userDto.getUsername());
        if (userWithSameUsername.isPresent() && userWithSameUsername.get().getId() != id) {
            throw new IllegalArgumentException("Username '" + userDto.getUsername() + "' is already taken.");
        }

        // Check if the email is already taken by another user
        Optional<User> userWithSameEmail = userRepo.findByEmail(userDto.getEmail());
        if (userWithSameEmail.isPresent() && userWithSameEmail.get().getId() != id) {
            throw new IllegalArgumentException("Email '" + userDto.getEmail() + "' is already taken.");
        }

        // Update the existing user with the new values from userDto
        updateExistingUser(existingUser, userDto);

        // Handle profile picture upload
        MultipartFile profilePictureFile = userDto.getProfilePicture();
        if (profilePictureFile != null && !profilePictureFile.isEmpty()) {
            String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(profilePictureFile.getOriginalFilename()));
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));

            // Generate a unique file name using UUID
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            Path uploadPath = Paths.get("D:/JAVA/pulse/uploads");

            try {
                Files.createDirectories(uploadPath); // Create directories if they don't exist
                Path filePath = uploadPath.resolve(uniqueFileName);
                Files.copy(profilePictureFile.getInputStream(), filePath);

                // Optionally delete the old profile picture file if it exists
                if (existingUser.getProfilePicture() != null) {
                    Path oldFilePath = uploadPath.resolve(existingUser.getProfilePicture());
                    Files.deleteIfExists(oldFilePath);
                }

                // Update the filename in the user object
                existingUser.setProfilePicture(uniqueFileName);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store image " + uniqueFileName, e);
            }
        }

        // Save updated User
        User updatedUser = userRepo.save(existingUser);

        // Convert to dto
        ConvertToDto(updatedUser);
    }


    @Override
    public UserDto getUserById(int id) {
        // Find User By id
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Convert to Dto
        UserDto userDto = ConvertToDto(user);

        // Append the image URL to the profile picture if it exists
        if (user.getProfilePicture() != null) {
            String profilePictureUrl = "/auth/pp/" + user.getProfilePicture(); // Base URL + filename
            userDto.setProfilePictureUrl(profilePictureUrl); // Assuming UserDto has this field
        }

        return userDto;
    }



    @Override
    public UserDto getUserByEmail(String email) {

        User user= userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User  not found"));

        // Convert to Dto
        return ConvertToDto(user);
    }

    @Override
    public UserDto getUserByUsername(String username) {
        // Find user by username
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username " + username));
        // Convert to Dto
        return ConvertToDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepo.findAll()
                .stream()
                .map(this::ConvertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(int id) {

        User user =userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));

        // Delete User
        userRepo.delete(user);
    }

    // Authenticate User
    @Transactional
    public UserDto authenticateUser(String identifier, String password) {
        User user = userRepo.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update user status to ACTIVE
        user.setStatus(Status.ACTIVE); // Set status to ACTIVE
        userRepo.saveAndFlush(user); // Ensure immediate flush to the database

        return ConvertToDto(user);
    }


    private User findUserByIdentifier(String identifier) {
        Optional<User> user;

        // Check if the identifier is an email or a phone number
        if (isEmail(identifier)) {
            user = userRepo.findByEmail(identifier);
        } else if (isPhoneNumber(identifier)) {
            user = userRepo.findByPhone(identifier);
        } else {
            user = userRepo.findByUsername(identifier);
        }

        return user.orElseThrow(() -> new RuntimeException("User not found"));
    }

    private boolean isEmail(String identifier) {
        String emailPattern = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return Pattern.compile(emailPattern).matcher(identifier).matches();
    }

    private boolean isPhoneNumber(String identifier) {
        String phonePattern = "^\\+?[0-9]{10,15}$";
        return Pattern.compile(phonePattern).matcher(identifier).matches();
    }


    @Override
    public Optional<User> findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    @Override
    public void setUserStatus(String username, Status status) {
        // FInd user by username
        User user= userRepo.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username " + username));

        // Set user status
        user.setStatus(status);

        //save updated user
        userRepo.save(user);
    }

    @Override
    public Optional<User> findById(int id) {
        return userRepo.findById(id);
    }

    public List<User> findAllUsers() {
        return userRepo.findAll();
    }



    // Convert to Entity
    private User ConvertToEntity(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPhone(userDto.getPhone());
        user.setAddress(userDto.getAddress());
        user.setGender(userDto.getGender());
        user.setRole(userDto.getRole());
        user.setStatus(Status.INACTIVE);
        user.setCreatedAt(Date.from(Instant.now()));
        user.setUpdatedAt(Date.from(Instant.now()));
        user.setProfilePicture(userDto.getProfilePicture() != null ? userDto.getProfilePictureUrl() : null);
        return user;
    }

    // Convert to Dto
    private UserDto ConvertToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setPassword(user.getPassword());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setPhone(user.getPhone());
        userDto.setAddress(user.getAddress());
        userDto.setGender(user.getGender());
        userDto.setRole(user.getRole());
        userDto.setStatus(user.getStatus());
        userDto.setCreatedAt(user.getCreatedAt());
        userDto.setUpdatedAt(user.getUpdatedAt());
        userDto.setProfilePictureUrl(user.getProfilePicture() != null ? user.getProfilePicture() : null);
        userDto.setProfilePicture(null); // MultipartFile can't be set from a String, so handle file retrieval in other ways if needed
        return userDto;
    }

    private void updateExistingUser(User existingUser, UserDto userDto){
        existingUser.setUsername(userDto.getUsername());
        existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        existingUser.setEmail(userDto.getEmail());
        existingUser.setFirstName(userDto.getFirstName());
        existingUser.setLastName(userDto.getLastName());
        existingUser.setPhone(userDto.getPhone());
        existingUser.setAddress(userDto.getAddress());
        existingUser.setGender(userDto.getGender());
        existingUser.setRole(userDto.getRole());
        existingUser.setStatus(userDto.getStatus());
        existingUser.setUpdatedAt(Date.from(Instant.now()));
    }

}
