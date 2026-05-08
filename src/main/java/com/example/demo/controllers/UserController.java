package com.example.demo.controllers;

import com.example.demo.dtos.requests.ReqPaginationDto;
import com.example.demo.dtos.requests.ReqUserUpdateDto;
import com.example.demo.dtos.responses.BaseResponse;
import com.example.demo.dtos.responses.ResPaginationDto;
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

       BaseResponse<ResUserDto> response = BaseResponse.success("User Data Retrieved Successfully", user, null);
       return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<ResUserDto>> getCurrentUserInformation() {
        UUID userId = SecurityUtils.getCurrentProfileId();
        ResUserDto user = userService.getUserById(userId);

        BaseResponse<ResUserDto> response = BaseResponse.success("Information Retrieved Successfully", user, null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/")
    public ResponseEntity<BaseResponse<Iterable<ResUserDto>>> listUsers(
            @Valid ReqPaginationDto paginationDto
    ) {
        // TODO: IMPLEMENT CHECKING ROLE
        Page<ResUserDto> users = userService.findAllUsers(paginationDto.getSize(), paginationDto.getPage());
        ResPaginationDto pagination = new ResPaginationDto(users.getSize(), users.getNumber());
        BaseResponse<Iterable<ResUserDto>> response = BaseResponse.success("Users Retrieved Successfully", users.getContent(), pagination);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me")
    public ResponseEntity<BaseResponse<Void>> updateUserInformation(
            @Valid @RequestBody ReqUserUpdateDto user
            ) {
        UUID userId = SecurityUtils.getCurrentProfileId();
        userService.updateUserById(user, userId);
        BaseResponse<Void> response = BaseResponse.success("User Information Updated Successfully", null, null);
        return ResponseEntity.ok(response);
    }

}
