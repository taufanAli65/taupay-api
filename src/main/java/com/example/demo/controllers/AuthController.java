package com.example.demo.controllers;

import com.example.demo.dtos.requests.ReqLoginDto;
import com.example.demo.dtos.requests.ReqRegisterDto;
import com.example.demo.dtos.requests.ReqRegisterMerchantDto;
import com.example.demo.dtos.responses.BaseResponse;
import com.example.demo.dtos.responses.ResLoginDto;
import com.example.demo.dtos.responses.ResRegisterDto;
import com.example.demo.dtos.responses.ResRegisterMerchantDto;
import com.example.demo.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "Public endpoints for account registration and login.")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a user", description = "Creates a new account with the USER role.")
    public ResponseEntity<BaseResponse<ResRegisterDto>> register(
            @Valid @RequestBody ReqRegisterDto request
    ) {
        ResRegisterDto user = authService.register(request);
        BaseResponse<ResRegisterDto> response = BaseResponse.success("Success Register User", user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/merchant")
    @Operation(summary = "Register a merchant", description = "Creates a new merchant account and profile.")
    public ResponseEntity<BaseResponse<ResRegisterMerchantDto>> registerMerchant(
            @Valid @RequestBody ReqRegisterMerchantDto request
    ) {
        ResRegisterMerchantDto merchant = authService.registerMerchant(request);
        BaseResponse<ResRegisterMerchantDto> response = BaseResponse.success("Success Register Merchant", merchant);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates a user, merchant, or super admin and returns a JWT.")
    public ResponseEntity<BaseResponse<ResLoginDto>> login(
            @Valid @RequestBody ReqLoginDto request
    ) {
        ResLoginDto user = authService.login(request);
        BaseResponse<ResLoginDto> response = BaseResponse.success("Success Login User", user);
        return ResponseEntity.ok(response);
    }
}
