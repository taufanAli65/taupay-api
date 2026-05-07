package com.example.demo.controllers;

import com.example.demo.dtos.requests.ReqIdDto;
import com.example.demo.dtos.responses.BaseResponse;
import com.example.demo.dtos.responses.ResUserDto;
import com.example.demo.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            @Valid @RequestParam(required = true) ReqIdDto id
            ) {
        // TODO: NEED TO IMPLEMENT CHECKING ROLE
        ResUserDto user = userService.getUserById(id);

       BaseResponse<ResUserDto> response = BaseResponse.success("User Data Retrieved Successfully", user);
       return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<ResUserDto>> getCurrentUserInformation(
            @Valid @RequestParam(required = true) ReqIdDto id
    ) {
        ResUserDto user = userService.getUserById(id);

        BaseResponse<ResUserDto> response = BaseResponse.success("Information Retrieved Successfully", user);
        return ResponseEntity.ok(response);
    }
}
