package com.example.demo.controllers;

import com.example.demo.dtos.requests.ReqCreateProductDto;
import com.example.demo.dtos.responses.*;
import com.example.demo.services.ProductService;
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
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<ResProductDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ResProductDto> productPage = productService.getAllProduct(page, size);
        ResPaginationDto pagination = new ResPaginationDto(productPage.getSize(), productPage.getNumber());

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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<ResCreateProductDto>> create(
            @RequestPart("data") @Valid ReqCreateProductDto request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        ResCreateProductDto product = productService.createProduct(request, file);
        return ResponseEntity.ok(BaseResponse.success("Product Created Successfully", product));
    }

    @PostMapping("/bulk")
    public ResponseEntity<BaseResponse<List<ResCreateProductDto>>> createBulk(
            @RequestBody @Valid List<ReqCreateProductDto> requests
    ) {
        List<ResCreateProductDto> products = productService.createBulkProducts(requests);
        return ResponseEntity.ok(BaseResponse.success("Bulk Products Created Successfully", products));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<ResCreateProductDto>> update(
            @PathVariable UUID id,
            @RequestPart("data") @Valid ReqCreateProductDto request,
            @RequestPart(value = "file", required = false) MultipartFile file
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
