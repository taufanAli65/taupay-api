package com.example.demo.controllers;

import com.example.demo.dtos.responses.BaseResponse;
import com.example.demo.dtos.responses.ResPaginationDto;
import com.example.demo.dtos.responses.ResProductDto;
import com.example.demo.services.ProductService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@Tag(name = "Admin Products", description = "Super admin endpoints for product management and lookups.")
@SecurityRequirement(name = "bearerAuth")
public class AdminProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<ResProductDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ResProductDto> productPage = productService.getAllProduct(page, size);
        ResPaginationDto pagination = new ResPaginationDto(productPage.getSize(), productPage.getNumber(), productPage.getTotalElements(), productPage.getTotalPages());

        return ResponseEntity.ok(BaseResponse.success("Products Retrieved Successfully", productPage.getContent(), pagination));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ResProductDto>> getProductById(
            @PathVariable("id") UUID id
    ) {
        ResProductDto merchantCategory = productService.getProductById(id);
        BaseResponse<ResProductDto> response = BaseResponse.success("Product Retrieved Successfully", merchantCategory);
        return ResponseEntity.ok(response);
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
