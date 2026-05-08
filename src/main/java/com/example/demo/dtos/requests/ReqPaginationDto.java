package com.example.demo.dtos.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ReqPaginationDto {
    @Min(value = 0, message = "page must be greater than or equal to 0")
    private Integer page;
    @Min(value = 1, message = "size must be greater than or equal to 1")
    @Max(value = 100, message = "size must be less than or equal to 100")
    private Integer size;
}
