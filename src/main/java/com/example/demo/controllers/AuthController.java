package com.example.demo.controllers;

import com.example.demo.dtos.requests.ReqLoginDto;
import com.example.demo.dtos.requests.ReqRegisterDto;
import com.example.demo.dtos.requests.ReqRegisterMerchantDto;
import com.example.demo.dtos.responses.BaseResponse;
import com.example.demo.dtos.responses.ResLoginDto;
import com.example.demo.dtos.responses.ResRegisterDto;
import com.example.demo.dtos.responses.ResRegisterMerchantDto;
import com.example.demo.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<ResRegisterDto>> register(
            @Valid @RequestBody ReqRegisterDto request
    ) {
        ResRegisterDto user = authService.register(request);
        BaseResponse<ResRegisterDto> response = BaseResponse.success("Success Register User", user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/merchant")
    public ResponseEntity<BaseResponse<ResRegisterMerchantDto>> registerMerchant(
            @Valid @RequestBody ReqRegisterMerchantDto request
    ) {
        ResRegisterMerchantDto merchant = authService.registerMerchant(request);
        BaseResponse<ResRegisterMerchantDto> response = BaseResponse.success("Success Register Merchant", merchant);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<ResLoginDto>> login(
            @Valid @RequestBody ReqLoginDto request
    ) {
        ResLoginDto loginResponse = authService.login(request);
        BaseResponse<ResLoginDto> response = BaseResponse.success("Success Login", loginResponse);
        return ResponseEntity.ok(response);
    }
}
