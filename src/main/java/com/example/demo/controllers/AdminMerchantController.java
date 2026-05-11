package com.example.demo.controllers;

import com.example.demo.dtos.requests.ReqMerchantCategoryDto;
import com.example.demo.dtos.requests.ReqMerchantDto;
import com.example.demo.dtos.requests.ReqMerchantStatusDto;
import com.example.demo.dtos.requests.ReqPaginationDto;
import com.example.demo.dtos.requests.ReqRegisterMerchantDto;
import com.example.demo.dtos.responses.BaseResponse;
import com.example.demo.dtos.responses.ResMerchantCategoryDto;
import com.example.demo.dtos.responses.ResMerchantDto;
import com.example.demo.dtos.responses.ResPaginationDto;
import com.example.demo.services.MerchantService;
import com.example.demo.services.MerchantCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequestMapping("/api/v1/admin/merchant")
@RequiredArgsConstructor
@Tag(name = "Admin Merchants", description = "Super admin endpoints for merchant and merchant category management.")
@SecurityRequirement(name = "bearerAuth")
public class AdminMerchantController {
    private final MerchantService merchantService;
    private final MerchantCategoryService merchantCategoryService;

    @PostMapping({"", "/"})
    @Operation(summary = "Create merchant", description = "Creates a merchant account and profile as a super admin.")
    public ResponseEntity<BaseResponse<ResMerchantDto>> createMerchant(
            @Valid @RequestBody ReqRegisterMerchantDto request
    ) {
        ResMerchantDto merchant = merchantService.createMerchant(request);
        BaseResponse<ResMerchantDto> response = BaseResponse.success("Merchant Created Successfully", merchant, null);
        return ResponseEntity.ok(response);
    }

    @GetMapping({"", "/"})
    @Operation(summary = "List merchants", description = "Returns a paginated list of merchants.")
    public ResponseEntity<BaseResponse<Iterable<ResMerchantDto>>> listMerchants(
            @ParameterObject @Valid ReqPaginationDto paginationDto
    ) {
        int size = paginationDto.getSize() == null ? 10 : paginationDto.getSize();
        int page = paginationDto.getPage() == null ? 0 : paginationDto.getPage();
        Page<ResMerchantDto> merchants = merchantService.findAllMerchants(size, page);
        ResPaginationDto pagination = new ResPaginationDto(merchants.getSize(), merchants.getNumber());
        BaseResponse<Iterable<ResMerchantDto>> response = BaseResponse.success("Merchants Retrieved Successfully", merchants.getContent(), pagination);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get merchant by ID", description = "Returns a merchant profile by its identifier.")
    public ResponseEntity<BaseResponse<ResMerchantDto>> getMerchantById(
            @PathVariable("id") UUID id
    ) {
        ResMerchantDto merchant = merchantService.getMerchantById(id);
        BaseResponse<ResMerchantDto> response = BaseResponse.success("Merchant Retrieved Successfully", merchant, null);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update merchant", description = "Updates a merchant profile by its identifier.")
    public ResponseEntity<BaseResponse<ResMerchantDto>> updateMerchant(
            @PathVariable("id") UUID id,
            @Valid @RequestBody ReqMerchantDto request
    ) {
        ResMerchantDto merchant = merchantService.updateMerchantById(id, request);
        BaseResponse<ResMerchantDto> response = BaseResponse.success("Merchant Updated Successfully", merchant, null);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update merchant status", description = "Activates or deactivates a merchant account.")
    public ResponseEntity<BaseResponse<ResMerchantDto>> updateMerchantStatus(
            @PathVariable("id") UUID id,
            @Valid @RequestBody ReqMerchantStatusDto request
    ) {
        ResMerchantDto merchant = merchantService.updateMerchantStatus(id, request);
        BaseResponse<ResMerchantDto> response = BaseResponse.success("Merchant Status Updated Successfully", merchant, null);
        return ResponseEntity.ok(response);
    }

    // Merchant Category Start Here
    @PostMapping("/category")
    @Operation(summary = "Create merchant category", description = "Creates a new merchant category.")
    public ResponseEntity<BaseResponse<ResMerchantCategoryDto>> createMerchantCategory(
            @Valid @RequestBody ReqMerchantCategoryDto request
    ) {
        ResMerchantCategoryDto merchantCategory = merchantCategoryService.createMerchantCategory(request);
        BaseResponse<ResMerchantCategoryDto> response = BaseResponse.success("Merchant Category Created Successfully", merchantCategory, null);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/category/{id}")
    @Operation(summary = "Update merchant category", description = "Updates the name of a merchant category.")
    public ResponseEntity<BaseResponse<Void>> updateMerchantCategoryName(
            @PathVariable("id") UUID id,
            @Valid @RequestBody ReqMerchantCategoryDto request
    ) {
        merchantCategoryService.updateMerchantCategoryName(id, request);
        BaseResponse<Void> response = BaseResponse.success("Merchant Category Updated Successfully", null);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/category/{id}")
    @Operation(summary = "Delete merchant category", description = "Deletes a merchant category by its identifier.")
    public ResponseEntity<BaseResponse<Void>> deleteMerchantCategory(
            @PathVariable("id") UUID id
    ) {
        merchantCategoryService.deleteMerchantCategory(id);
        BaseResponse<Void> response = BaseResponse.success("Merchant Category Deleted Successfully", null);
        return ResponseEntity.ok(response);
    }
}
