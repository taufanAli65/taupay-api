package com.example.demo.mappers;

import com.example.demo.dtos.requests.ReqCreateProductDto;
import com.example.demo.dtos.responses.*;
import com.example.demo.entities.MerchantEntity;
import com.example.demo.entities.ProductEntity;
import com.example.demo.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {
    private final FileService fileService;

    public ProductEntity toEntity(ReqCreateProductDto dto) {
        if (dto == null) {
            return null;
        }

        ProductEntity entity = new ProductEntity();
        entity.setName(dto.getName());
        entity.setPrice(dto.getPrice());
        entity.setDescription(dto.getDescription());
        entity.setIsActive(true);
        return entity;
    }

    public ResCreateProductDto toCreateResponse(ProductEntity product) {
        ResCreateProductDto response = new ResCreateProductDto();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        response.setDescription(product.getDescription());
        response.setIsActive(product.getIsActive());

        if (product.getQuantityEntity() != null) {
            response.setStock(product.getQuantityEntity().getStock());
        }

        response.setMerchantId(product.getMerchant().getId());
        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());
        }
        return response;
    }

    public ResProductDto toProductResponse(ProductEntity product, MerchantEntity merchant) {
        ResProductDto response = new ResProductDto();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        response.setDescription(product.getDescription());
        response.setIsActive(product.getIsActive());

        if (product.getQuantityEntity() != null) {
            response.setStock(product.getQuantityEntity().getStock());
        }

        if (product.getImageName() != null) {
            try {
                response.setImageUrl(fileService.getFileUrl(product.getImageName()));
            } catch (RuntimeException e) {
                response.setImageUrl(null);
            }
        }

        if (merchant != null) {
            ResMerchantDto merchantDto = new ResMerchantDto();
            merchantDto.setId(merchant.getId());
            merchantDto.setName(merchant.getName());
            merchantDto.setEmail(merchant.getAccount().getEmail());
            merchantDto.setAddress(merchant.getAddress());
            merchantDto.setCategoryName(merchant.getCategory().getName());
            merchantDto.setCategoryId(merchant.getCategory().getId());
            merchantDto.setActive(merchant.getIsActive());
            response.setMerchant(merchantDto);
        }

        if (product.getCategory() != null) {
            ResProductCategoryDto categoryDto = new ResProductCategoryDto();
            categoryDto.setId(product.getCategory().getId());
            categoryDto.setName(product.getCategory().getName());
            response.setCategory(categoryDto);
        }

        return response;
    }
}
