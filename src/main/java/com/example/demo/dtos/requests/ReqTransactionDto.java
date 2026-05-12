package com.example.demo.dtos.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(name = "ReqTransactionDto", description = "Transaction request payload.")
public class ReqTransactionDto {

    @Schema(description = "List of products purchased")
    private List<ProductItem> products;

    @Data
    @Schema(name = "ReqTransactionProductItem", description = "Details of a product in the transaction.")
    public static class ProductItem {

        @JsonProperty("product_id")
        @Schema(description = "ID of the product", example = "7902ce2f-6a44-4738-a7fa-6a4d1737cf7f")
        private String productId;

        @Schema(description = "Quantity purchased", example = "2")
        private Integer quantity;
    }
}