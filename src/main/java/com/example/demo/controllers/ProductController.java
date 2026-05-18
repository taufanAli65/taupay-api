package com.example.demo.controllers;

import com.example.demo.dtos.requests.ReqCreateProductDto;
import com.example.demo.dtos.requests.ReqProductFilterDto;
import com.example.demo.dtos.responses.*;
import com.example.demo.services.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Endpoints for product management and lookups.")
@SecurityRequirement(name = "bearerAuth")
@org.springframework.validation.annotation.Validated
public class ProductController {
    private final ProductService productService;

    @GetMapping
    @Operation(summary = "List active products", description = "Returns a paginated list of active products for the authenticated merchant with optional search and filters.")
    public ResponseEntity<BaseResponse<List<ResProductDto>>> getAll(
            @ParameterObject @Valid ReqProductFilterDto filterDto
    ) {
        int size = filterDto.getSize() == null ? 10 : filterDto.getSize();
        int page = filterDto.getPage() == null ? 0 : filterDto.getPage();
        Page<ResProductDto> productPage = productService.getAllProduct(filterDto, page, size);
        ResPaginationDto pagination = new ResPaginationDto(productPage.getSize(), productPage.getNumber(), productPage.getTotalElements(), productPage.getTotalPages());

        return ResponseEntity.ok(BaseResponse.success("Products Retrieved Successfully", productPage.getContent(), pagination));
    }

    @GetMapping("/deactivated")
    @Operation(summary = "List deactivated products", description = "Returns a paginated list of deactivated products for the authenticated merchant.")
    public ResponseEntity<BaseResponse<List<ResProductDto>>> getDeactivatedProducts(
            @ParameterObject @Valid ReqProductFilterDto filterDto
    ) {
        int size = filterDto.getSize() == null ? 10 : filterDto.getSize();
        int page = filterDto.getPage() == null ? 0 : filterDto.getPage();
        Page<ResProductDto> productPage = productService.findDeactivatedProducts(filterDto, page, size);
        ResPaginationDto pagination = new ResPaginationDto(productPage.getSize(), productPage.getNumber(), productPage.getTotalElements(), productPage.getTotalPages());

        return ResponseEntity.ok(BaseResponse.success("Deactivated Products Retrieved Successfully", productPage.getContent(), pagination));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get product statistics", description = "Returns counts for total, active, and deactivated products for the authenticated merchant.")
    public ResponseEntity<BaseResponse<ResProductStatisticsDto>> getStatistics() {
        ResProductStatisticsDto statistics = productService.getProductStatistics();
        return ResponseEntity.ok(BaseResponse.success("Product Statistics Retrieved Successfully", statistics));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ResProductDto>> getProductById(
            @PathVariable("id") UUID id
    ) {
        ResProductDto merchantCategory = productService.getProductById(id);
        BaseResponse<ResProductDto> response = BaseResponse.success("Product Retrieved Successfully", merchantCategory);
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<ResCreateProductDto>> create(
            @ModelAttribute @Valid ReqCreateProductDto request,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        ResCreateProductDto product = productService.createProduct(request, file);
        return ResponseEntity.ok(BaseResponse.success("Product Created Successfully", product));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<ResCreateProductDto>> update(
            @PathVariable UUID id,
            @ModelAttribute @Valid ReqCreateProductDto request,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        ResCreateProductDto product = productService.updateProduct(id, request, file);
        return ResponseEntity.ok(BaseResponse.success("Product Updated Successfully", product));
    }

    @PatchMapping("/{id}/deactive")
    public ResponseEntity<BaseResponse<Object>> deactivate(
            @PathVariable UUID id
    ) {
        productService.deactivateProduct(id);
        return ResponseEntity.ok(BaseResponse.success("Product Deactivated Successfully", null));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<BaseResponse<Object>> activate(
            @PathVariable UUID id
    ) {
        productService.activateProduct(id);
        return ResponseEntity.ok(BaseResponse.success("Product Activate Successfully", null));
    }
}
