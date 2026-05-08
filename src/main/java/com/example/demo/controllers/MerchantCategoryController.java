package com.example.demo.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.requests.ReqMerchantCategoryDto;
import com.example.demo.dtos.responses.BaseResponse;
import com.example.demo.dtos.responses.ResMerchantCategoryDto;
import com.example.demo.services.MerchantCategoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/merchant-category")
public class MerchantCategoryController {
    private final MerchantCategoryService merchantCategoryService;

    public MerchantCategoryController(MerchantCategoryService merchantCategoryService) {
        this.merchantCategoryService = merchantCategoryService;
    }

    @GetMapping("/")
    public ResponseEntity<BaseResponse<List<ResMerchantCategoryDto>>> getAllMerchantCategories() {
        List<ResMerchantCategoryDto> merchantCategories = merchantCategoryService.getAllMerchantCategories();
        BaseResponse<List<ResMerchantCategoryDto>> response = BaseResponse.success("Merchant Categories Retrieved Successfully", merchantCategories, null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ResMerchantCategoryDto>> getMerchantCategoryById(
            @PathVariable("id") UUID id
    ) {
        ResMerchantCategoryDto merchantCategory = merchantCategoryService.getMerchantCategoryById(id);
        BaseResponse<ResMerchantCategoryDto> response = BaseResponse.success("Merchant Category Retrieved Successfully", merchantCategory, null);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BaseResponse<ResMerchantCategoryDto>> updateMerchantCategoryName(
            @PathVariable("id") UUID id,
            @Valid @RequestBody ReqMerchantCategoryDto request
    ) {
        merchantCategoryService.updateMerchantCategoryName(id, request);
        BaseResponse<ResMerchantCategoryDto> response = BaseResponse.success("Merchant Category Updated Successfully", null, null);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteMerchantCategory(
            @PathVariable("id") UUID id
    ) {
        merchantCategoryService.deleteMerchantCategory(id);
        BaseResponse<Void> response = BaseResponse.success("Merchant Category Deleted Successfully", null, null);
        return ResponseEntity.ok(response);
    }
}
