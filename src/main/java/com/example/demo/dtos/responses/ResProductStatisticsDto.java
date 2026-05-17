package com.example.demo.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ResProductStatisticsDto", description = "Product count statistics for a merchant.")
public class ResProductStatisticsDto {
    @Schema(description = "Total number of products owned by the merchant.", example = "100")
    private Long totalProducts;

    @Schema(description = "Number of active products.", example = "95")
    private Long activeProducts;

    @Schema(description = "Number of deactivated products.", example = "5")
    private Long deactivatedProducts;
}
