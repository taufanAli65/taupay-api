package com.example.demo.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(name = "ReqPaginationDto", description = "Pagination query parameters.")
public class ReqPaginationDto {
    @Min(value = 0, message = "page must be greater than or equal to 0")
    @Schema(description = "Zero-based page index. Defaults to 0 when omitted.", example = "0")
    private Integer page;

    @Min(value = 1, message = "size must be greater than or equal to 1")
    @Max(value = 100, message = "size must be less than or equal to 100")
    @Schema(description = "Number of items per page (acts as limit). Defaults to 10 when omitted.", example = "10")
    private Integer size;

    @Schema(description = "Field name to sort by.", example = "createdAt")
    private String sortBy;

    @Schema(description = "Sort direction: ASC or DESC.", example = "DESC")
    private String sortDir;
}
