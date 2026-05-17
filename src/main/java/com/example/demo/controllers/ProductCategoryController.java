package com.example.demo.controllers;

import com.example.demo.dtos.requests.ReqProductCategoryDto;
import com.example.demo.dtos.responses.BaseResponse;
import com.example.demo.dtos.responses.ResProductCategoryDto;
import com.example.demo.services.ProductCategoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products/categories")
@RequiredArgsConstructor
@Tag(name = "Product Categories", description = "Endpoints for product category self-service")
@SecurityRequirement(name = "bearerAuth")
public class ProductCategoryController {
    private final ProductCategoryService productCategoryService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<ResProductCategoryDto>>> getAll(
            @RequestParam(required = false) String search
    ) {
        List<ResProductCategoryDto> categories = productCategoryService.getAllProductCategories(search);
        return ResponseEntity.ok(BaseResponse.success("Product Category Retrieved Successfully", categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ResProductCategoryDto>> getById(
            @PathVariable("id") UUID id
    ) {
        ResProductCategoryDto category = productCategoryService.getProductCategoryById(id);
        return ResponseEntity.ok(BaseResponse.success("Category Retrieved Successfully", category));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<ResProductCategoryDto>> create(
            @RequestBody @Valid ReqProductCategoryDto request
    ) {
        ResProductCategoryDto category = productCategoryService.createProductCategory(request);
        return ResponseEntity.ok(BaseResponse.success("Category Created Successfully", category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<ResProductCategoryDto>> update(
            @PathVariable UUID id, @RequestBody @Valid ReqProductCategoryDto request
    ) {
        ResProductCategoryDto category = productCategoryService.updateProductCategory(id, request);
        return ResponseEntity.ok(BaseResponse.success("Category Updated Successfully", category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Object>> delete(
            @PathVariable UUID id
    ) {
        productCategoryService.deleteProductCategory(id);
        return ResponseEntity.ok(BaseResponse.success("Category Deleted Successfully", null));
    }
}
