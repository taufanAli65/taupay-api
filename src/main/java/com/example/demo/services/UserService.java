package com.example.demo.services;

import com.example.demo.dtos.requests.ReqUserUpdateDto;
import com.example.demo.dtos.responses.ResUserDto;

import java.util.UUID;

public interface UserService {
    ResUserDto getUserById(UUID id);
    void updateUserById(ReqUserUpdateDto request, UUID user_id);
}
