package com.example.demo.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "ReqProductFilterDto", description = "Query parameters for filtering products.")
public class ReqProductFilterDto extends ReqPaginationDto {
    @Schema(description = "Search by name, price, stock, category name, or merchant name.", example = "kopi")
    private String search;

    @Schema(description = "Filter by active status.", example = "true")
    private Boolean isActive;

    @Schema(description = "Filter by product category ID.", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    private UUID categoryId;

    @Schema(description = "Filter by stock availability (true for stock > 0, false for stock = 0).", example = "true")
    private Boolean inStock;
}
