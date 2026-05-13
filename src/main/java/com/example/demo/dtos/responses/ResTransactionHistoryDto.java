package com.example.demo.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResTransactionHistoryDto {
    private UUID trxId;
    private Long amount;
    private String counterpartyName;
    private String category;
    private LocalDateTime createdAt;
    private List<ProductDetail> products;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductDetail {
        private String name;
        private Integer quantity;
        private Long priceAtTime;
    }
}
