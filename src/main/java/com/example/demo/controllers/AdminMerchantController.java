package com.example.demo.controllers;

import com.example.demo.dtos.requests.ReqMerchantDto;
import com.example.demo.dtos.requests.ReqMerchantStatusDto;
import com.example.demo.dtos.requests.ReqPaginationDto;
import com.example.demo.dtos.requests.ReqRegisterMerchantDto;
import com.example.demo.dtos.responses.BaseResponse;
import com.example.demo.dtos.responses.ResMerchantDto;
import com.example.demo.dtos.responses.ResPaginationDto;
import com.example.demo.services.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/merchants")
@RequiredArgsConstructor
public class AdminMerchantController {
    private final MerchantService merchantService;

    @PostMapping({"", "/"})
    public ResponseEntity<BaseResponse<ResMerchantDto>> createMerchant(
            @Valid @RequestBody ReqRegisterMerchantDto request
    ) {
        ResMerchantDto merchant = merchantService.createMerchant(request);
        BaseResponse<ResMerchantDto> response = BaseResponse.success("Merchant Created Successfully", merchant, null);
        return ResponseEntity.ok(response);
    }

    @GetMapping({"", "/"})
    public ResponseEntity<BaseResponse<Iterable<ResMerchantDto>>> listMerchants(
            @Valid ReqPaginationDto paginationDto
    ) {
        int size = paginationDto.getSize() == null ? 10 : paginationDto.getSize();
        int page = paginationDto.getPage() == null ? 0 : paginationDto.getPage();
        Page<ResMerchantDto> merchants = merchantService.findAllMerchants(size, page);
        ResPaginationDto pagination = new ResPaginationDto(merchants.getSize(), merchants.getNumber());
        BaseResponse<Iterable<ResMerchantDto>> response = BaseResponse.success("Merchants Retrieved Successfully", merchants.getContent(), pagination);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ResMerchantDto>> getMerchantById(
            @PathVariable("id") UUID id
    ) {
        ResMerchantDto merchant = merchantService.getMerchantById(id);
        BaseResponse<ResMerchantDto> response = BaseResponse.success("Merchant Retrieved Successfully", merchant, null);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<ResMerchantDto>> updateMerchant(
            @PathVariable("id") UUID id,
            @Valid @RequestBody ReqMerchantDto request
    ) {
        ResMerchantDto merchant = merchantService.updateMerchantById(id, request);
        BaseResponse<ResMerchantDto> response = BaseResponse.success("Merchant Updated Successfully", merchant, null);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BaseResponse<ResMerchantDto>> updateMerchantStatus(
            @PathVariable("id") UUID id,
            @Valid @RequestBody ReqMerchantStatusDto request
    ) {
        ResMerchantDto merchant = merchantService.updateMerchantStatus(id, request);
        BaseResponse<ResMerchantDto> response = BaseResponse.success("Merchant Status Updated Successfully", merchant, null);
        return ResponseEntity.ok(response);
    }
}
