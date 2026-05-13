package com.example.demo.services.impl;

import com.example.demo.dtos.requests.ReqUserUpdateDto;
import com.example.demo.dtos.responses.ResUserDto;
import com.example.demo.entities.UserEntity;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.UserService;
import lombok.extern.slf4j.Slf4j;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import com.example.demo.utils.PartialUpdateUtils;
import com.example.demo.utils.SecurityUtils;
import com.example.demo.exceptions.UnauthorizedException;

import java.util.UUID;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public ResUserDto getUserById(UUID user_id) {
        return userRepository.findUserById(user_id).orElseThrow(
                () -> new DataNotFoundException("User with ID: " + user_id + " not found")
        );
    }

    @Override
    public void updateUserById(ReqUserUpdateDto request, UUID user_id) {
        UserEntity user = userRepository.findById(user_id).orElseThrow(
                () -> new DataNotFoundException("User with ID: " + user_id + " not found")
        );
        PartialUpdateUtils.copyNonNullProperties(request, user);
        userRepository.save(user);
    }

    @Override
    public Page<ResUserDto> findAllUsers(int size, int page) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return userRepository.findAllUsers(pageRequest);
    }

    @Override
    public void toggleUserStatus(UUID user_id, boolean isActive) {
        if (SecurityUtils.hasRole("SUPER_ADMIN")) {
            UserEntity user = userRepository.findById(user_id).orElseThrow(
                    () -> new DataNotFoundException("User with ID: " + user_id + " not found")
            );
            user.setIsActive(isActive);
            userRepository.save(user);
        } else {
            throw new UnauthorizedException("Only SUPER_ADMIN can toggle user status"); 
        } // TODO: INVALIDATE USER SESSION UNTILL ADMIN RE-ACTIVATE THE USER ACCOUNT OR TTL FOR DEACTIVATION
    }
}
