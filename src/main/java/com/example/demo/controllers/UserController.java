package com.example.demo.controllers;

import com.example.demo.dtos.requests.ReqChangePinDto;
import com.example.demo.dtos.requests.ReqPaginationDto;
import com.example.demo.dtos.requests.ReqUserFilterDto;
import com.example.demo.dtos.requests.ReqUserUpdateDto;
import com.example.demo.dtos.responses.BaseResponse;
import com.example.demo.dtos.responses.ResPaginationDto;
import com.example.demo.dtos.responses.ResTransactionHistoryDto;
import com.example.demo.dtos.responses.ResUserDto;
import com.example.demo.services.TransactionService;
import com.example.demo.services.UserService;
import com.example.demo.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for user profile access and administration.")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;
    private final TransactionService transactionService;
    
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns the authenticated USER profile based on the JWT profile ID.")
    public ResponseEntity<BaseResponse<ResUserDto>> getCurrentUserInformation() {
        UUID userId = SecurityUtils.getCurrentProfileId();
        ResUserDto user = userService.getUserById(userId);

        BaseResponse<ResUserDto> response = BaseResponse.success("Information Retrieved Successfully", user, null);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping
    @Operation(summary = "List users", description = "Returns a paginated list of all user profiles for super admins.")
    public ResponseEntity<BaseResponse<Iterable<ResUserDto>>> listUsers(
            @ParameterObject @Valid ReqUserFilterDto filterDto
    ) {
        Page<ResUserDto> users = userService.findAllUsers(filterDto);
        ResPaginationDto pagination = new ResPaginationDto(users.getSize(), users.getNumber(), users.getTotalElements(), users.getTotalPages());
        BaseResponse<Iterable<ResUserDto>> response = BaseResponse.success("Users Retrieved Successfully", users.getContent(), pagination);
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

    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/me/pin")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Change user PIN", description = "Updates the user PIN. Requires old PIN if already set.")
    public ResponseEntity<BaseResponse<Void>> changePin(
            @Valid @RequestBody ReqChangePinDto request
    ) {
        UUID userId = SecurityUtils.getCurrentProfileId();
        userService.changePin(userId, request);
        return ResponseEntity.ok(BaseResponse.success("PIN Updated Successfully", null));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/transactions")
    @Operation(summary = "Get transaction history", description = "Returns a paginated list of transactions for the current user.")
    public ResponseEntity<BaseResponse<java.util.List<ResTransactionHistoryDto>>> getTransactionHistory(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @ParameterObject @Valid ReqPaginationDto paginationDto
    ) {
        UUID userId = SecurityUtils.getCurrentProfileId();
        Page<ResTransactionHistoryDto> history = transactionService.getTransactionHistory(userId, startDate, endDate, paginationDto, false);
        ResPaginationDto pagination = new ResPaginationDto(history.getSize(), history.getNumber(), history.getTotalElements(), history.getTotalPages());
        return ResponseEntity.ok(BaseResponse.success("Transaction History Retrieved", history.getContent(), pagination));
    }
}
