package com.example.demo.services;

import com.example.demo.dtos.requests.ReqChangePinDto;
import com.example.demo.dtos.requests.ReqMerchantDto;
import com.example.demo.dtos.requests.ReqMerchantFilterDto;
import com.example.demo.dtos.requests.ReqMerchantStatusDto;
import com.example.demo.dtos.requests.ReqRegisterMerchantDto;
import com.example.demo.dtos.responses.ResCommonStatisticsDto;
import com.example.demo.dtos.responses.ResMerchantDashboardDto;
import com.example.demo.dtos.responses.ResMerchantDto;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.UUID;

public interface MerchantService {
    ResMerchantDto createMerchant(ReqRegisterMerchantDto request);

    Page<ResMerchantDto> findAllMerchants(ReqMerchantFilterDto filterDto);

    ResMerchantDto getMerchantById(UUID merchantId);

    ResMerchantDto updateMerchantById(UUID merchantId, ReqMerchantDto request);

    ResMerchantDto updateMerchantStatus(UUID merchantId, ReqMerchantStatusDto request);

    void lockPayments(UUID merchantId, LocalDateTime lockedUntil);

    ResCommonStatisticsDto getAdminMerchantStatistics();

    ResMerchantDashboardDto getMerchantDashboard(UUID merchantId);

    void changePin(UUID merchantId, ReqChangePinDto request);
}
