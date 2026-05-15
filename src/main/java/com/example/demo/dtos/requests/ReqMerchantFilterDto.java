package com.example.demo.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "ReqMerchantFilterDto", description = "Query parameters for filtering merchants.")
public class ReqMerchantFilterDto extends ReqPaginationDto {
    @Schema(description = "Search by name, email, address, or category name.", example = "fashion")
    private String search;

    @Schema(description = "Filter by active status.", example = "true")
    private Boolean isActive;

    @Schema(description = "Filter by merchant category ID.", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    private UUID categoryId;
}
