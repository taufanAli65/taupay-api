package com.example.demo.services.impl;

import com.example.demo.dtos.requests.ReqUserFilterDto;
import com.example.demo.dtos.requests.ReqUserUpdateDto;
import com.example.demo.dtos.responses.ResCommonStatisticsDto;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.example.demo.utils.PartialUpdateUtils;
import com.example.demo.utils.SecurityUtils;
import com.example.demo.exceptions.UnauthorizedException;

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
        
        PageRequest pageRequest;
        if (filterDto.getSortBy() != null && !filterDto.getSortBy().isBlank()) {
            Sort.Direction direction = (filterDto.getSortDir() != null && filterDto.getSortDir().equalsIgnoreCase("ASC")) 
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            pageRequest = PageRequest.of(page, size, Sort.by(direction, filterDto.getSortBy()));
        } else {
            pageRequest = PageRequest.of(page, size);
        }

        Specification<UserEntity> spec = UserSpecification.filterBy(filterDto);

        return userRepository.findAll(spec, pageRequest).map(userMapper::toUserResponse);
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

    @Override
    public ResCommonStatisticsDto getAdminUserStatistics() {
        return ResCommonStatisticsDto.builder()
                .total(userRepository.count())
                .active(userRepository.countByIsActiveTrue())
                .deactivated(userRepository.countByIsActiveFalse())
                .build();
    }
}
