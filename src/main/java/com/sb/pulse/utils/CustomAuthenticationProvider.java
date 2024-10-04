package com.sb.pulse.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CustomAuthenticationProvider(CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String identifier = authentication.getName();
        String password = authentication.getCredentials().toString();

        try {
            UserDetails userDetails = loadUserByIdentifier(identifier);
            validatePassword(password, userDetails.getPassword());

            return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
        } catch (BadCredentialsException e) {
            logger.error("Invalid credentials for user: {}", identifier);
            throw e;
        } catch (UsernameNotFoundException e) {
            logger.error("User not found: {}", identifier);
            throw e;
        }
    }


    private UserDetails loadUserByIdentifier(String identifier) {
        UserDetails userDetails = null;

        if (userDetailsService.isEmail(identifier)) {
            userDetails = userDetailsService.loadUserByEmail(identifier);
        } else if (userDetailsService.isPhoneNumber(identifier)) {
            userDetails = userDetailsService.loadUserByPhoneNumber(identifier);
        } else {
            userDetails = userDetailsService.loadUserByUsername(identifier);
        }

        if (userDetails == null) {
            throw new BadCredentialsException("User not found");
        }

        return userDetails;
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
