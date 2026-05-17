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
@Schema(name = "ResCommonStatisticsDto", description = "Generic count statistics.")
public class ResCommonStatisticsDto {
    @Schema(description = "Total number of records.", example = "100")
    private Long total;

    @Schema(description = "Number of active records.", example = "95")
    private Long active;

    @Schema(description = "Number of deactivated records.", example = "5")
    private Long deactivated;
}
