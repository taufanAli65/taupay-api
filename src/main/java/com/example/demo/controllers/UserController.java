package com.example.demo.controllers;

import com.example.demo.dtos.requests.ReqUserUpdateDto;
import com.example.demo.dtos.responses.BaseResponse;
import com.example.demo.dtos.responses.ResUserDto;
import com.example.demo.services.UserService;
import com.example.demo.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ResUserDto>> getUserById(
            @Valid @PathVariable("id") UUID id
            ) {
        ResUserDto user = userService.getUserById(id);
        BaseResponse<ResUserDto> response = BaseResponse.success("User Data Retrieved Successfully", user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<ResUserDto>> getCurrentUserInformation() {
        UUID userId = SecurityUtils.getCurrentProfileId();
        ResUserDto user = userService.getUserById(userId);

        BaseResponse<ResUserDto> response = BaseResponse.success("Information Retrieved Successfully", user);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me")
    public ResponseEntity<BaseResponse> updateUserInformation(
            @Valid @RequestBody ReqUserUpdateDto user
            ) {
        UUID userId = SecurityUtils.getCurrentProfileId();
        userService.updateUserById(user, userId);
        BaseResponse response = BaseResponse.success("User Information Updated Successfully", null);
        return ResponseEntity.ok(response);
    }

}
