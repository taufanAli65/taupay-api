package com.example.demo.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ResMerchantCategoryDto", description = "Merchant category response payload.")
public class ResMerchantCategoryDto {
    @Schema(description = "Merchant category identifier.", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    private UUID id;

    @Schema(description = "Merchant category name.", example = "Food & Beverage")
    private String name;
}
