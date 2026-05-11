package com.example.demo.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReqProductCategoryDto {
    @NotBlank(message = "Category name is required")
    private String name;
}
