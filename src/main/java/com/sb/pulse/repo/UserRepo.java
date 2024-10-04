package com.sb.pulse.repo;

import com.sb.pulse.enums.Status;
import com.sb.pulse.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Integer> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String identifier);

    Optional<User> findByUsernameOrEmail(String identifier, String identifier1);


}
