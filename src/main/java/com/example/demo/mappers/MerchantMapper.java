package com.example.demo.mappers;

import com.example.demo.dtos.responses.ResMerchantDto;
import com.example.demo.dtos.responses.ResRegisterMerchantDto;
import com.example.demo.entities.MerchantEntity;
import org.springframework.stereotype.Component;

@Component
public class MerchantMapper {
    public ResMerchantDto toResponse(MerchantEntity merchant) {
        if (merchant == null) {
            return null;
        }

        return ResMerchantDto.builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .email(merchant.getAccount().getEmail())
                .address(merchant.getAddress())
                .categoryId(merchant.getCategory().getId())
                .categoryName(merchant.getCategory().getName())
                .active(merchant.getIsActive())
                .build();
    }

    public ResRegisterMerchantDto toRegisterResponse(MerchantEntity merchant) {
        if (merchant == null) {
            return null;
        }

        ResRegisterMerchantDto response = new ResRegisterMerchantDto();
        response.setId(merchant.getId());
        response.setName(merchant.getName());
        response.setEmail(merchant.getAccount().getEmail());
        response.setAddress(merchant.getAddress());
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
        response.setId(merchant.getId());
        response.setName(merchant.getName());
        response.setEmail(merchant.getEmail());
        response.setAddress(merchant.getAddress());
        response.setCategory(merchant.getCategoryName());
        return response;
    }
}
