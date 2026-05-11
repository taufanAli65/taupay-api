package com.example.demo.controllers;

import com.example.demo.dtos.requests.ReqMerchantDto;
import com.example.demo.dtos.responses.BaseResponse;
import com.example.demo.dtos.responses.ResMerchantCategoryDto;
import com.example.demo.dtos.responses.ResMerchantDto;
import com.example.demo.services.MerchantService;
import com.example.demo.services.MerchantCategoryService;
import com.example.demo.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchant")
@RequiredArgsConstructor
@Tag(name = "Merchants", description = "Endpoints for merchant self-service and merchant category lookups.")
@SecurityRequirement(name = "bearerAuth")
public class MerchantController {
    private final MerchantService merchantService;
    private final MerchantCategoryService merchantCategoryService;

    @PreAuthorize("hasRole('MERCHANT')")
    @GetMapping({"", "/me"})
    @Operation(summary = "Get current merchant", description = "Returns the authenticated merchant profile from the JWT profile ID.")
    public ResponseEntity<BaseResponse<ResMerchantDto>> getCurrentMerchant() {
        UUID merchantId = SecurityUtils.getCurrentProfileId();
        ResMerchantDto merchant = merchantService.getMerchantById(merchantId);
        BaseResponse<ResMerchantDto> response = BaseResponse.success("Merchant Retrieved Successfully", merchant, null);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @PutMapping({"", "/me"})
    @Operation(summary = "Update current merchant", description = "Updates the authenticated merchant profile.")
    public ResponseEntity<BaseResponse<ResMerchantDto>> updateCurrentMerchant(
            @Valid @RequestBody ReqMerchantDto request
    ) {
        UUID merchantId = SecurityUtils.getCurrentProfileId();
        ResMerchantDto merchant = merchantService.updateMerchantById(merchantId, request);
        BaseResponse<ResMerchantDto> response = BaseResponse.success("Merchant Updated Successfully", merchant, null);
        return ResponseEntity.ok(response);
    }

    // Merchant Category Start Here
    @GetMapping("/category")
    @Operation(summary = "List merchant categories", description = "Returns all available merchant categories.")
    public ResponseEntity<BaseResponse<List<ResMerchantCategoryDto>>> getAllMerchantCategories() {
        List<ResMerchantCategoryDto> merchantCategories = merchantCategoryService.getAllMerchantCategories();
        BaseResponse<List<ResMerchantCategoryDto>> response = BaseResponse.success("Merchant Categories Retrieved Successfully", merchantCategories);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{id}")
    @Operation(summary = "Get merchant category by ID", description = "Returns a single merchant category by its identifier.")
    public ResponseEntity<BaseResponse<ResMerchantCategoryDto>> getMerchantCategoryById(
            @PathVariable("id") UUID id
    ) {
        ResMerchantCategoryDto merchantCategory = merchantCategoryService.getMerchantCategoryById(id);
        BaseResponse<ResMerchantCategoryDto> response = BaseResponse.success("Merchant Category Retrieved Successfully", merchantCategory);
        return ResponseEntity.ok(response);
    }
}
