package com.example.demo.mappers;

import com.example.demo.dtos.responses.ResDailyRevenueDto;
import com.example.demo.dtos.responses.ResDashboardFinancialDto;
import com.example.demo.dtos.responses.ResLowStockProductDto;
import com.example.demo.dtos.responses.ResTopProductDto;
import com.example.demo.entities.ProductEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class MerchantDashboardMapper {

    public ResDashboardFinancialDto toFinancial(
            Long todayRevenue,
            Long todayOrders,
            Long yesterdayRevenue,
            Long yesterdayOrders,
            Long averageOrderValue,
            Long activeProducts,
            Long totalProducts,
            Long deactivatedProducts,
            Long lowStockCount
    ) {
        ResDashboardFinancialDto financial = new ResDashboardFinancialDto();
        financial.setTodayRevenue(valueOrZero(todayRevenue));
        financial.setTodayOrders(valueOrZero(todayOrders));
        financial.setYesterdayRevenue(valueOrZero(yesterdayRevenue));
        financial.setYesterdayOrders(valueOrZero(yesterdayOrders));
        financial.setAverageOrderValue(valueOrZero(averageOrderValue));
        financial.setActiveProducts(valueOrZero(activeProducts));
        financial.setTotalProducts(valueOrZero(totalProducts));
        financial.setDeactivatedProducts(valueOrZero(deactivatedProducts));
        financial.setLowStockCount(valueOrZero(lowStockCount));
        return financial;
    }

    public List<ResTopProductDto> toTopProducts(List<Object[]> rows) {
        List<ResTopProductDto> topProducts = new ArrayList<>();
        if (rows == null) {
            return topProducts;
        }

        for (Object[] row : rows) {
            topProducts.add(toTopProduct(row));
        }
        return topProducts;
    }

    public ResTopProductDto toTopProduct(Object[] row) {
        ResTopProductDto dto = new ResTopProductDto();
        if (row == null) {
            return dto;
        }

        dto.setProductName(row.length > 0 && row[0] != null ? row[0].toString() : null);
        dto.setTotalQuantity(row.length > 1 && row[1] != null ? ((Number) row[1]).longValue() : 0L);
        dto.setTotalRevenue(row.length > 2 && row[2] != null ? ((Number) row[2]).longValue() : 0L);
        return dto;
    }

    public List<ResDailyRevenueDto> toDailyRevenueTrend(Map<String, Long> dailyRevenueMap, LocalDate startDate, int days) {
        List<ResDailyRevenueDto> trend = new ArrayList<>();
        if (dailyRevenueMap == null) {
            dailyRevenueMap = Map.of();
        }

        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            String key = date.toString();
            trend.add(toDailyRevenue(key, dailyRevenueMap.getOrDefault(key, 0L)));
        }
        return trend;
    }

    public ResDailyRevenueDto toDailyRevenue(String date, Long revenue) {
        ResDailyRevenueDto dto = new ResDailyRevenueDto();
        dto.setDate(date);
        dto.setRevenue(valueOrZero(revenue));
        return dto;
    }

    public List<ResLowStockProductDto> toLowStockProducts(List<ProductEntity> products) {
        List<ResLowStockProductDto> lowStockProducts = new ArrayList<>();
        if (products == null) {
            return lowStockProducts;
        }

        for (ProductEntity product : products) {
            lowStockProducts.add(toLowStockProduct(product));
        }
        return lowStockProducts;
    }

    public ResLowStockProductDto toLowStockProduct(ProductEntity product) {
        ResLowStockProductDto dto = new ResLowStockProductDto();
        if (product == null) {
            return dto;
        }

        dto.setProductId(product.getId() != null ? product.getId().toString() : null);
        dto.setProductName(product.getName());
        dto.setStock(product.getQuantityEntity() != null ? product.getQuantityEntity().getStock() : 0);
        return dto;
    }

    private Long valueOrZero(Long value) {
        return value != null ? value : 0L;
    }
}