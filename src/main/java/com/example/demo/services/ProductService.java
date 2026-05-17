package com.example.demo.services;
import com.example.demo.dtos.requests.ReqCreateProductDto;
import com.example.demo.dtos.requests.ReqProductFilterDto;
import com.example.demo.dtos.responses.ResCreateProductDto;
import com.example.demo.dtos.responses.ResProductDto;
import com.example.demo.dtos.responses.ResProductStatisticsDto;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    Page<ResProductDto> getAllProduct(ReqProductFilterDto filterDto, int page, int size);

    Page<ResProductDto> findDeactivatedProducts(ReqProductFilterDto filterDto, int page, int size);

    Page<ResProductDto> findAllProducts(ReqProductFilterDto filterDto);

    Page<ResProductDto> getProductsByMerchantId(UUID merchantId, int page, int size);

    ResProductDto getProductById(UUID id);

    ResCreateProductDto createProduct(ReqCreateProductDto request, MultipartFile file);

    ResCreateProductDto updateProduct(UUID id, ReqCreateProductDto request, MultipartFile file);

    ResProductStatisticsDto getProductStatistics();

    void deactivateProduct(UUID id);

    void activateProduct(UUID id);
}
