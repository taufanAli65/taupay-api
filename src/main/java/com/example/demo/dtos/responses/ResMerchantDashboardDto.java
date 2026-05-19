package com.example.demo.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(name = "ResMerchantDashboardDto", description = "Composite merchant dashboard payload")
public class ResMerchantDashboardDto {
    private ResDashboardFinancialDto financial;
    private List<ResDailyRevenueDto> revenueTrend;
    private List<ResTopProductDto> topProducts;
    private List<ResLowStockProductDto> lowStockProducts;
}
