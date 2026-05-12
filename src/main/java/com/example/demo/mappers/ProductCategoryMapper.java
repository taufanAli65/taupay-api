package com.example.demo.mappers;

import com.example.demo.dtos.requests.ReqProductCategoryDto;
import com.example.demo.dtos.responses.ResProductCategoryDto;
import com.example.demo.entities.MerchantEntity;
import com.example.demo.entities.ProductCategoryEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductCategoryMapper extends BaseMapper<ProductCategoryEntity, ReqProductCategoryDto, ResProductCategoryDto> {
    
    @Override
    public ProductCategoryEntity toEntity(ReqProductCategoryDto dto) {
        if (dto == null) {
            return null;
        }

        ProductCategoryEntity entity = new ProductCategoryEntity();
        map(dto, entity);
        return entity;
    }

    @Override
    public ResProductCategoryDto toResponse(ProductCategoryEntity category) {
        if (category == null) {
            return null;
        }
        ResProductCategoryDto response = new ResProductCategoryDto();
        map(category, response);
        return response;
    }

    public ResProductCategoryDto toProductCategoryResponse(ProductCategoryEntity category, MerchantEntity merchant) {
        ResProductCategoryDto response = toResponse(category);
        if (merchant != null && response != null) {
            response.setMerchantId(merchant.getId());
            response.setMerchantName(merchant.getName());
        }
        return response;
    }
}
