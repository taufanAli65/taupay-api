package com.example.demo.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "ResDashboardFinancialDto", description = "Financial metrics for merchant dashboard")
public class ResDashboardFinancialDto {
    private Long todayRevenue = 0L;
    private Long todayOrders = 0L;
    private Long yesterdayRevenue = 0L;
    private Long yesterdayOrders = 0L;
    private Long averageOrderValue = 0L;
    private Long activeProducts = 0L;
    private Long totalProducts = 0L;
    private Long deactivatedProducts = 0L;
    private Long lowStockCount = 0L;
}
