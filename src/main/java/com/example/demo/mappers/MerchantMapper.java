package com.example.demo.mappers;

import com.example.demo.dtos.responses.ResMerchantDto;
import com.example.demo.dtos.responses.ResRegisterMerchantDto;
import com.example.demo.entities.MerchantEntity;
import org.springframework.stereotype.Component;

@Component
public class MerchantMapper extends BaseMapper<MerchantEntity, Object, ResMerchantDto> {

    @Override
    public MerchantEntity toEntity(Object dto) {
        return null; // Not implemented
    }

    @Override
    public ResMerchantDto toResponse(MerchantEntity merchant) {
        if (merchant == null) {
            return null;
        }
        ResMerchantDto response = new ResMerchantDto();
        map(merchant, response);
        response.setActive(merchant.getIsActive());
        
        if (merchant.getAccount() != null) {
            response.setEmail(merchant.getAccount().getEmail());
            response.setIsPinSet(merchant.getAccount().getPin() != null && !merchant.getAccount().getPin().isBlank());
        }
        
        if (merchant.getCategory() != null) {
            response.setCategoryId(merchant.getCategory().getId());
            response.setCategoryName(merchant.getCategory().getName());
        }
        
        return response;
    }

    public ResRegisterMerchantDto toRegisterResponse(MerchantEntity merchant) {
        if (merchant == null) {
            return null;
        }

        ResRegisterMerchantDto response = new ResRegisterMerchantDto();
        map(merchant, response);
        
        if (merchant.getAccount() != null) {
            response.setEmail(merchant.getAccount().getEmail());
        }
        
        if (merchant.getCategory() != null) {
            response.setCategory(merchant.getCategory().getName());
        }
        return response;
    }

    public ResRegisterMerchantDto toRegisterResponse(ResMerchantDto merchant) {
        if (merchant == null) {
            return null;
        }

        ResRegisterMerchantDto response = new ResRegisterMerchantDto();
        map(merchant, response);
        response.setCategory(merchant.getCategoryName());
        return response;
    }
}
