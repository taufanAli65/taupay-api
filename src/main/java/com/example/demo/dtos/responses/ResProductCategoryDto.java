package com.example.demo.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResProductCategoryDto {
    private UUID id;
    private String name;
    private UUID merchantId;
    private String merchantName;
}
