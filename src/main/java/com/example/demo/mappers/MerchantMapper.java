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

        return ResRegisterMerchantDto.builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .email(merchant.getAccount().getEmail())
                .address(merchant.getAddress())
                .build();
    }

    public ResRegisterMerchantDto toRegisterResponse(ResMerchantDto merchant) {
        if (merchant == null) {
            return null;
        }

        return ResRegisterMerchantDto.builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .email(merchant.getEmail())
                .address(merchant.getAddress())
                .build();
    }
}
