package com.example.demo.services.impl;

import com.example.demo.dtos.requests.ReqUserFilterDto;
import com.example.demo.dtos.requests.ReqUserUpdateDto;
import com.example.demo.dtos.responses.ResUserDto;
import com.example.demo.entities.UserEntity;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.mappers.UserMapper;
import com.example.demo.repositories.UserRepository;
import com.example.demo.repositories.specs.UserSpecification;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.example.demo.utils.PartialUpdateUtils;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

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
    public Page<ResUserDto> findAllUsers(ReqUserFilterDto filterDto) {
        int size = filterDto.getSize() != null ? filterDto.getSize() : 10;
        int page = filterDto.getPage() != null ? filterDto.getPage() : 0;
        PageRequest pageRequest = PageRequest.of(page, size);

        Specification<UserEntity> spec = UserSpecification.filterBy(filterDto);

        return userRepository.findAll(spec, pageRequest).map(userMapper::toUserResponse);
    }
}
