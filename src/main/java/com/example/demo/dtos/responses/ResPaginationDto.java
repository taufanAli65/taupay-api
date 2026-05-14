package com.example.demo.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ResPaginationDto", description = "Pagination metadata.")
public class ResPaginationDto {
    @Schema(description = "Number of items returned in the current page.", example = "10")
    private int size;

    @Schema(description = "Zero-based page index returned by the API.", example = "0")
    private int page;

    @Schema(description = "Total number of elements across all pages.", example = "100")
    private long totalElements;

    @Schema(description = "Total number of pages available.", example = "10")
    private int totalPages;
}
