package com.example.demo.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "ResTopProductDto", description = "Top product sold summary")
public class ResTopProductDto {
    private String productName;
    private Long totalQuantity = 0L;
    private Long totalRevenue = 0L;
}
