package com.example.demo.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "ReqProductFilterDto", description = "Query parameters for filtering products.")
public class ReqProductFilterDto extends ReqPaginationDto {
    @Schema(description = "Search by name, price, stock, category name, or merchant name.", example = "kopi")
    private String search;

    @Schema(description = "Filter by active status.", example = "true")
    private Boolean isActive;
}
