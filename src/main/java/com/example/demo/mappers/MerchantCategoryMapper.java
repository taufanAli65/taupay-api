package com.example.demo.mappers;

import com.example.demo.dtos.responses.ResMerchantCategoryDto;
import com.example.demo.entities.MerchantCategoryEntity;
import org.springframework.stereotype.Component;

@Component
public class MerchantCategoryMapper {
    public ResMerchantCategoryDto toResponse(MerchantCategoryEntity merchantCategory) {
        if (merchantCategory == null) {
            return null;
        }

        return new ResMerchantCategoryDto(merchantCategory.getId(), merchantCategory.getName());
    }
}
