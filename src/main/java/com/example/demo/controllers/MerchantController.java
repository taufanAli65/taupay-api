package com.example.demo.controllers;

import com.example.demo.dtos.requests.ReqMerchantDto;
import com.example.demo.dtos.responses.BaseResponse;
import com.example.demo.dtos.responses.ResMerchantDto;
import com.example.demo.services.MerchantService;
import com.example.demo.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchant/me")
@RequiredArgsConstructor
public class MerchantController {
    private final MerchantService merchantService;

    @GetMapping({"", "/"})
    public ResponseEntity<BaseResponse<ResMerchantDto>> getCurrentMerchant() {
        UUID merchantId = SecurityUtils.getCurrentProfileId();
        ResMerchantDto merchant = merchantService.getMerchantById(merchantId);
        BaseResponse<ResMerchantDto> response = BaseResponse.success("Merchant Retrieved Successfully", merchant, null);
        return ResponseEntity.ok(response);
    }

    @PutMapping({"", "/"})
    public ResponseEntity<BaseResponse<ResMerchantDto>> updateCurrentMerchant(
            @Valid @RequestBody ReqMerchantDto request
    ) {
        UUID merchantId = SecurityUtils.getCurrentProfileId();
        ResMerchantDto merchant = merchantService.updateMerchantById(merchantId, request);
        BaseResponse<ResMerchantDto> response = BaseResponse.success("Merchant Updated Successfully", merchant, null);
        return ResponseEntity.ok(response);
    }
}
