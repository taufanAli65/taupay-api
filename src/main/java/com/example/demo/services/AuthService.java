package com.example.demo.services;

import com.example.demo.dtos.requests.ReqLoginDto;
import com.example.demo.dtos.requests.ReqRegisterDto;
import com.example.demo.dtos.requests.ReqRegisterMerchantDto;
import com.example.demo.dtos.responses.ResLoginDto;
import com.example.demo.dtos.responses.ResRegisterDto;
import com.example.demo.dtos.responses.ResRegisterMerchantDto;

public interface AuthService {
    ResLoginDto login(ReqLoginDto dto);

    void logout(String token);

    ResRegisterDto register(ReqRegisterDto dto);

    ResRegisterMerchantDto registerMerchant(ReqRegisterMerchantDto dto);
}
