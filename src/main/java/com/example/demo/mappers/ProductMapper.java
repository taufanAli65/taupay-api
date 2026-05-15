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
public class ProductMapper extends BaseMapper<ProductEntity, ReqCreateProductDto, ResProductDto> {
    private final FileService fileService;

    @Override
    public ProductEntity toEntity(ReqCreateProductDto dto) {
        if (dto == null) {
            return null;
        }

        ProductEntity entity = new ProductEntity();
        map(dto, entity);
        entity.setIsActive(true);
        return entity;
    }

    @Override
    public ResProductDto toResponse(ProductEntity entity) {
        if (entity == null) {
            return null;
        }
        ResProductDto response = new ResProductDto();
        map(entity, response);
        
        if (entity.getQuantityEntity() != null) {
            response.setStock(entity.getQuantityEntity().getStock());
        }

        if (entity.getImageName() != null) {
            response.setImageUrl(fileService.getPublicUrl("products", entity.getImageName()));
        }

        if (entity.getCategory() != null) {
            ResProductCategoryDto categoryDto = new ResProductCategoryDto();
            map(entity.getCategory(), categoryDto);
            response.setCategory(categoryDto);
        }
        
        return response;
    }

    public ResCreateProductDto toCreateResponse(ProductEntity product) {
        if (product == null) return null;
        ResCreateProductDto response = new ResCreateProductDto();
        map(product, response);

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
        ResProductDto response = toResponse(product);
        if (response == null) return null;

        if (merchant != null) {
            ResMerchantDto merchantDto = new ResMerchantDto();
            map(merchant, merchantDto);
            merchantDto.setEmail(merchant.getAccount().getEmail());
            merchantDto.setCategoryName(merchant.getCategory().getName());
            merchantDto.setCategoryId(merchant.getCategory().getId());
            merchantDto.setActive(merchant.getIsActive());
            response.setMerchant(merchantDto);
        }

        if (product.getCategory() != null) {
            ResProductCategoryDto categoryDto = new ResProductCategoryDto();
            map(product.getCategory(), categoryDto);
            response.setCategory(categoryDto);
        }

        return response;
    }
}
