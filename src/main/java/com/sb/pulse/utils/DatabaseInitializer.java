package com.sb.pulse.utils;

import com.sb.pulse.enums.Gender;
import com.sb.pulse.enums.Role;
import com.sb.pulse.enums.Status;
import com.sb.pulse.model.User;
import com.sb.pulse.repo.UserRepo;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class DatabaseInitializer {


    private final UserRepo userRepository;

    @Autowired
    public DatabaseInitializer(UserRepo userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        if (!userRepository.existsByEmail("sumanbisunkhe304@gmail.com")) {
            User user = new User();
            user.setUsername("Suman");
            user.setPassword("$2a$12$CgeWqCls7y1lOl4U7umNEeBNoSUExhG2dgfJseWY27O.jlHnCKt8e");
            user.setEmail("sumanbisunkhe304@gmail.com");
            user.setFirstName("Suman");
            user.setLastName("Bisunkhe");
            user.setPhone("9840948274");
            user.setAddress("Kathmandu, Nepal");
            user.setProfilePicture("prof.jpg");
            user.setGender(Gender.MALE);
            user.setRole(Role.ADMIN);
            user.setStatus(Status.INACTIVE);
            user.setCreatedAt(new Date());
            user.setUpdatedAt(null);
            userRepository.save(user);
        }
    }
}
