package com.example.demo.services.impl;

import com.example.demo.dtos.requests.ReqUserUpdateDto;
import com.example.demo.dtos.responses.ResUserDto;
import com.example.demo.entities.UserEntity;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.example.demo.utils.PartialUpdateUtils;

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
        UserEntity user = userRepository.findById(user_id).orElseThrow(
                () -> new DataNotFoundException("User with ID: " + user_id + " not found")
        );
        return new ResUserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getAddress(),
                user.getBirthDate(),
                user.getIsActive(),
                user.getMerchant()
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
}
