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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasRole('SUPER_ADMIN') or (hasRole('USER') and @securityUtils.isCurrentProfileId(#id))")
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ResUserDto>> getUserById(
            @Valid @PathVariable("id") UUID id
    ) {
        ResUserDto user = userService.getUserById(id);

        BaseResponse<ResUserDto> response = BaseResponse.success("User Data Retrieved Successfully", user, null);
        return ResponseEntity.ok(response);
       BaseResponse<ResUserDto> response = BaseResponse.success("User Data Retrieved Successfully", user, null);
       return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<ResUserDto>> getCurrentUserInformation() {
        UUID userId = SecurityUtils.getCurrentProfileId();
        ResUserDto user = userService.getUserById(userId);

        BaseResponse<ResUserDto> response = BaseResponse.success("Information Retrieved Successfully", user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/")
    public ResponseEntity<BaseResponse<Iterable<ResUserDto>>> listUsers(
            @Valid ReqPaginationDto paginationDto
    ) {
        int size = paginationDto.getSize() == null ? 10 : paginationDto.getSize();
        int page = paginationDto.getPage() == null ? 0 : paginationDto.getPage();
        Page<ResUserDto> users = userService.findAllUsers(size, page);
        ResPaginationDto pagination = new ResPaginationDto(users.getSize(), users.getNumber());
        BaseResponse<Iterable<ResUserDto>> response = BaseResponse.success("Users Retrieved Successfully", users.getContent(), pagination);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/me")
    public ResponseEntity<BaseResponse<Void>> updateUserInformation(
            @Valid @RequestBody ReqUserUpdateDto user
    ) {
        UUID userId = SecurityUtils.getCurrentProfileId();
        userService.updateUserById(user, userId);
        BaseResponse<Void> response = BaseResponse.success("User Information Updated Successfully", null);
        return ResponseEntity.ok(response);
    }
}
