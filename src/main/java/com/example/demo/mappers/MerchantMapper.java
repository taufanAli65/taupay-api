package com.example.demo.mappers;

import com.example.demo.dtos.requests.ReqRegisterMerchantDto;
import com.example.demo.dtos.responses.ResRegisterMerchantDto;
import com.example.demo.entities.AccountEntity;
import com.example.demo.entities.MerchantCategoryEntity;
import com.example.demo.entities.MerchantEntity;
import org.springframework.stereotype.Component;

@Component
public class MerchantMapper {

    public MerchantEntity toEntity(ReqRegisterMerchantDto dto, MerchantCategoryEntity category) {
        if (dto == null) {
            return null;
        }

        MerchantEntity entity = new MerchantEntity();
        entity.setName(dto.getName());
        entity.setAddress(dto.getAddress());
        entity.setCategory(category);
        entity.setIsActive(true);
        return entity;
    }

    public ResRegisterMerchantDto toRegisterResponse(MerchantEntity merchant, AccountEntity account) {
        if (merchant == null) {
            return null;
        }

        ResRegisterMerchantDto response = new ResRegisterMerchantDto();
        response.setId(merchant.getId());
        response.setName(merchant.getName());
        response.setAddress(merchant.getAddress());
        if (account != null) {
            response.setEmail(account.getEmail());
        }
        if (merchant.getCategory() != null) {
            response.setCategory(merchant.getCategory().getName());
        }
        return response;
    }
}
