package com.example.demo.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "ResLowStockProductDto", description = "Product low stock info")
public class ResLowStockProductDto {
    private String productId;
    private String productName;
    private Integer stock;
}
