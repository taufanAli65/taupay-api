package com.example.demo.mappers;

import com.example.demo.dtos.responses.ResMerchantCategoryDto;
import com.example.demo.entities.MerchantCategoryEntity;
import org.springframework.stereotype.Component;

@Component
public class MerchantCategoryMapper extends BaseMapper<MerchantCategoryEntity, Object, ResMerchantCategoryDto> {
    
    @Override
    public MerchantCategoryEntity toEntity(Object dto) {
        return null; // Not implemented as not needed currently
    }

    @Override
    public ResMerchantCategoryDto toResponse(MerchantCategoryEntity merchantCategory) {
        if (merchantCategory == null) {
            return null;
        }
        ResMerchantCategoryDto response = new ResMerchantCategoryDto(null, null);
        map(merchantCategory, response);
        return response;
    }
}
