package com.example.demo.controllers;

import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.requests.ReqPaginationDto;
import com.example.demo.dtos.responses.BaseResponse;
import com.example.demo.dtos.responses.ResPaginationDto;
import com.example.demo.dtos.responses.ResUserDto;
import com.example.demo.services.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin Users", description = "Super admin endpoints for user management and lookups.")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {
    private final UserService userService;

    @GetMapping
    @Operation(summary = "List users", description = "Returns a paginated list of all user profiles for super admins.")
    public ResponseEntity<BaseResponse<Iterable<ResUserDto>>> listUsers(
            @ParameterObject @Valid ReqPaginationDto paginationDto
    ) {
        int size = paginationDto.getSize() == null ? 10 : paginationDto.getSize();
        int page = paginationDto.getPage() == null ? 0 : paginationDto.getPage();
        Page<ResUserDto> users = userService.findAllUsers(size, page);
        ResPaginationDto pagination = new ResPaginationDto(users.getSize(), users.getNumber());
        BaseResponse<Iterable<ResUserDto>> response = BaseResponse.success("Users Retrieved Successfully", users.getContent(), pagination);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Accessible to super admins or the user who owns the requested profile.")
    public ResponseEntity<BaseResponse<ResUserDto>> getUserById(
            @Valid @PathVariable("id") UUID id
    ) {
        ResUserDto user = userService.getUserById(id);

        BaseResponse<ResUserDto> response = BaseResponse.success("User Data Retrieved Successfully", user, null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate user", description = "Deactivates a user profile by ID, accessible to super admins.")
    public ResponseEntity<BaseResponse<Void>> deactivateUser(
            @Valid @PathVariable("id") UUID id
    ) {
        userService.toggleUserStatus(id, false);
        BaseResponse<Void> response = BaseResponse.success("User Deactivated Successfully", null, null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/activate")
    @Operation(summary = "Activate user", description = "Re-activates a user profile by ID, accessible to super admins.")
    public ResponseEntity<BaseResponse<Void>> activateUser(
            @Valid @PathVariable("id") UUID id
    ) {
        userService.toggleUserStatus(id, true);
        BaseResponse<Void> response = BaseResponse.success("User Activated Successfully", null, null);
        return ResponseEntity.ok(response);
    }

}
