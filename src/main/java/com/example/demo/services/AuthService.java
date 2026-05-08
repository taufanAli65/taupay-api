package com.example.demo.services;

import com.example.demo.dtos.requests.ReqLoginDto;
import com.example.demo.dtos.requests.ReqRegisterDto;
import com.example.demo.dtos.responses.ResLoginDto;
import com.example.demo.dtos.responses.ResRegisterDto;

public interface AuthService {
    ResLoginDto login(ReqLoginDto dto);

    ResRegisterDto register(ReqRegisterDto dto);
}
