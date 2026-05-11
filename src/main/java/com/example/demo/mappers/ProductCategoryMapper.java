package com.example.demo.mappers;

import com.example.demo.dtos.requests.ReqProductCategoryDto;
import com.example.demo.dtos.responses.ResProductCategoryDto;
import com.example.demo.entities.MerchantEntity;
import com.example.demo.entities.ProductCategoryEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductCategoryMapper {
    public ProductCategoryEntity toEntity(ReqProductCategoryDto dto) {
        if (dto == null) {
            return null;
        }

        ProductCategoryEntity entity = new ProductCategoryEntity();
        entity.setName(dto.getName());
        return entity;
    }

    public ResProductCategoryDto toProductCategoryResponse(ProductCategoryEntity product, MerchantEntity merchant) {
        ResProductCategoryDto response = new ResProductCategoryDto();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setMerchantId(merchant.getId());
        response.setMerchantName(merchant.getName());
        return response;
    }
}
