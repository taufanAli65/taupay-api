package com.example.demo.controllers;

import com.example.demo.dtos.requests.ReqMerchantCategoryDto;
import com.example.demo.dtos.responses.BaseResponse;
import com.example.demo.dtos.responses.ResMerchantCategoryDto;
import com.example.demo.services.MerchantCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchant-category")
public class MerchantCategoryController {
    private final MerchantCategoryService merchantCategoryService;

    public MerchantCategoryController(MerchantCategoryService merchantCategoryService) {
        this.merchantCategoryService = merchantCategoryService;
    }

    @PostMapping({"", "/"})
    public ResponseEntity<BaseResponse<ResMerchantCategoryDto>> createMerchantCategory(
            @Valid @RequestBody ReqMerchantCategoryDto request
    ) {
        ResMerchantCategoryDto merchantCategory = merchantCategoryService.createMerchantCategory(request);
        BaseResponse<ResMerchantCategoryDto> response = BaseResponse.success("Merchant Category Created Successfully", merchantCategory, null);
        return ResponseEntity.ok(response);
    }

    @GetMapping({"", "/"})
    public ResponseEntity<BaseResponse<List<ResMerchantCategoryDto>>> getAllMerchantCategories() {
        List<ResMerchantCategoryDto> merchantCategories = merchantCategoryService.getAllMerchantCategories();
        BaseResponse<List<ResMerchantCategoryDto>> response = BaseResponse.success("Merchant Categories Retrieved Successfully", merchantCategories);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ResMerchantCategoryDto>> getMerchantCategoryById(
            @PathVariable("id") UUID id
    ) {
        ResMerchantCategoryDto merchantCategory = merchantCategoryService.getMerchantCategoryById(id);
        BaseResponse<ResMerchantCategoryDto> response = BaseResponse.success("Merchant Category Retrieved Successfully", merchantCategory);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> updateMerchantCategoryName(
            @PathVariable("id") UUID id,
            @Valid @RequestBody ReqMerchantCategoryDto request
    ) {
        merchantCategoryService.updateMerchantCategoryName(id, request);
        BaseResponse<Void> response = BaseResponse.success("Merchant Category Updated Successfully", null);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteMerchantCategory(
            @PathVariable("id") UUID id
    ) {
        merchantCategoryService.deleteMerchantCategory(id);
        BaseResponse<Void> response = BaseResponse.success("Merchant Category Deleted Successfully", null);
        return ResponseEntity.ok(response);
    }
}
