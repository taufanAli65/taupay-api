package com.example.demo.services;

import com.example.demo.dtos.requests.ReqProductCategoryDto;
import com.example.demo.dtos.responses.ResProductCategoryDto;

import java.util.List;
import java.util.UUID;

public interface ProductCategoryService {
    List<ResProductCategoryDto> getAllProductCategories();

    ResProductCategoryDto getProductCategoryById(UUID id);

    ResProductCategoryDto createProductCategory(ReqProductCategoryDto request);

    ResProductCategoryDto updateProductCategory(UUID id, ReqProductCategoryDto request);

    void deleteProductCategory(UUID id);
}
