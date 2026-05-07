package com.example.demo.controllers;

import com.example.demo.dtos.requests.ReqUserUpdateDto;
import com.example.demo.dtos.responses.BaseResponse;
import com.example.demo.dtos.responses.ResUserDto;
import com.example.demo.entities.BaseEntity;
import com.example.demo.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;
    private final HttpServletRequest request;

    public UserController(UserService userService, HttpServletRequest request) {
        this.userService = userService;
        this.request = request;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ResUserDto>> getUserById(
            @Valid @PathVariable("id") UUID id
            ) {
        // TODO: IMPLEMENT CHECKING ROLE
        ResUserDto user = userService.getUserById(id);

       BaseResponse<ResUserDto> response = BaseResponse.success("User Data Retrieved Successfully", user);
       return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<ResUserDto>> getCurrentUserInformation() {
        UUID user_id = UUID.fromString(request.getHeader("X-Authenticated-User-Id"));
        ResUserDto user = userService.getUserById(user_id);

        BaseResponse<ResUserDto> response = BaseResponse.success("Information Retrieved Successfully", user);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<BaseResponse> updateUserInformation(
            @Valid @RequestBody ReqUserUpdateDto user
            ) {
        UUID user_id = UUID.fromString(request.getHeader("X-Authenticated-User-Id"));
        userService.updateUserById(user, user_id);
        BaseResponse response = BaseResponse.success("User Information Updated Successfully", null);
        return ResponseEntity.ok(response);
    }

}
