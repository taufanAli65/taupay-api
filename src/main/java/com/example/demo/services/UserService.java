package com.example.demo.services;

import com.example.demo.dtos.requests.ReqIdDto;
import com.example.demo.dtos.responses.ResUserDto;

import java.util.UUID;

public interface UserService {
    ResUserDto getUserById(ReqIdDto id);
}
