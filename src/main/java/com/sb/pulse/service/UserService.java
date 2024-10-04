package com.sb.pulse.service;

import com.sb.pulse.dto.UserDto;
import com.sb.pulse.enums.Status;
import com.sb.pulse.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    UserDto createUser(UserDto userDto);
    void updateUser(int id, UserDto userDto);
    UserDto getUserById(int id);
    UserDto getUserByEmail(String email);
    UserDto getUserByUsername(String username);
    List<UserDto> getAllUsers();
    void deleteUser(int id);
    UserDto authenticateUser(String username, String password);
    public Optional<User> findByUsername(String username);
    void setUserStatus(String username, Status status);

    Optional<User> findById(int id);

    List<User> findAllUsers();
}
