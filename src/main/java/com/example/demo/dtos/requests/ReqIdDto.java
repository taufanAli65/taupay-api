package com.example.demo.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReqIdDto {
    @NotNull(message = "ID is required and cannot be null")
    private UUID id;
}
