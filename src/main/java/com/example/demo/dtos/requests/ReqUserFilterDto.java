package com.example.demo.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "ReqUserFilterDto", description = "Query parameters for filtering users.")
public class ReqUserFilterDto extends ReqPaginationDto {
    @Schema(description = "Search by name, email, or address.", example = "john")
    private String search;

    @Schema(description = "Filter by active status.", example = "true")
    private Boolean isActive;
}
