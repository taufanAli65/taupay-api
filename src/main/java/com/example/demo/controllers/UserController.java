package com.example.demo.controllers;

import com.example.demo.dtos.requests.ReqUserUpdateDto;
import com.example.demo.dtos.responses.BaseResponse;
import com.example.demo.dtos.responses.ResUserDto;
import com.example.demo.services.UserService;
import com.example.demo.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for user profile access and administration.")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns the authenticated USER profile based on the JWT profile ID.")
    public ResponseEntity<BaseResponse<ResUserDto>> getCurrentUserInformation() {
        UUID userId = SecurityUtils.getCurrentProfileId();
        ResUserDto user = userService.getUserById(userId);

        BaseResponse<ResUserDto> response = BaseResponse.success("Information Retrieved Successfully", user, null);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/me")
    @Operation(summary = "Update current user", description = "Partially updates the authenticated USER profile.")
    public ResponseEntity<BaseResponse<Void>> updateUserInformation(
            @Valid @RequestBody ReqUserUpdateDto user
    ) {
        UUID userId = SecurityUtils.getCurrentProfileId();
        userService.updateUserById(user, userId);
        BaseResponse<Void> response = BaseResponse.success("User Information Updated Successfully", null, null);
        return ResponseEntity.ok(response);
    }
}
