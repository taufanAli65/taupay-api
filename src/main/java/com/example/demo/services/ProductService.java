package com.example.demo.services;

import com.example.demo.dtos.requests.ReqCreateProductDto;
import com.example.demo.dtos.responses.ResCreateProductDto;
import com.example.demo.dtos.responses.ResMerchantDto;
import com.example.demo.dtos.responses.ResProductDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    Page<ResProductDto> getAllProduct(int page, int size);

    ResProductDto getProductById(UUID id);

    ResCreateProductDto createProduct(ReqCreateProductDto request);

    List<ResCreateProductDto> createBulkProducts(List<ReqCreateProductDto> requests);

    ResCreateProductDto updateProduct(UUID id, ReqCreateProductDto request);

    void deactivateProduct(UUID id);

    void activateProduct(UUID id);
}
