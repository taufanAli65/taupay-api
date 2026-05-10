package com.example.demo.services;

import com.example.demo.dtos.requests.ReqMerchantDto;
import com.example.demo.dtos.requests.ReqMerchantStatusDto;
import com.example.demo.dtos.requests.ReqRegisterMerchantDto;
import com.example.demo.dtos.responses.ResMerchantDto;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface MerchantService {
    ResMerchantDto createMerchant(ReqRegisterMerchantDto request);

    Page<ResMerchantDto> findAllMerchants(int size, int page);

    ResMerchantDto getMerchantById(UUID merchantId);

    ResMerchantDto updateMerchantById(UUID merchantId, ReqMerchantDto request);

    ResMerchantDto updateMerchantStatus(UUID merchantId, ReqMerchantStatusDto request);
}
