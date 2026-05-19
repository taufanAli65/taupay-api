package com.example.demo.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "ResDailyRevenueDto", description = "Daily revenue point")
public class ResDailyRevenueDto {
    private String date;
    private Long revenue = 0L;
}
