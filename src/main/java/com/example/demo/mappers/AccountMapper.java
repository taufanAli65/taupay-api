package com.example.demo.mappers;

import com.example.demo.dtos.requests.ReqRegisterDto;
import com.example.demo.dtos.requests.ReqRegisterMerchantDto;
import com.example.demo.dtos.responses.ResLoginDto;
import com.example.demo.dtos.responses.ResRegisterDto;
import com.example.demo.dtos.responses.ResRegisterMerchantDto;
import com.example.demo.entities.AccountEntity;
import com.example.demo.entities.MerchantEntity;
import com.example.demo.entities.RoleEnum;
import com.example.demo.entities.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountMapper {
    private final UserMapper userMapper;
    private final MerchantMapper merchantMapper;

    public AccountEntity toEntity(ReqRegisterDto dto) {
        if (dto == null) {
            return null;
        }

        AccountEntity entity = new AccountEntity();
        entity.setEmail(dto.getEmail());
        entity.setRole(RoleEnum.USER);
        return entity;
    }

    public AccountEntity toEntity(ReqRegisterMerchantDto dto) {
        if (dto == null) {
            return null;
        }

        AccountEntity entity = new AccountEntity();
        entity.setEmail(dto.getEmail());
        entity.setRole(RoleEnum.MERCHANT);
        return entity;
    }

    public ResLoginDto toLoginResponse(AccountEntity account, UserEntity user, String token) {
        ResLoginDto response = new ResLoginDto();
        if (user != null) {
            ResRegisterDto userDto = userMapper.toRegisterResponse(user, account);
            response.setUser(userDto);
        }
        response.setToken(token);

        return response;
    }

    public ResLoginDto toLoginResponse(AccountEntity account, MerchantEntity merchant, String token) {
        ResLoginDto response = new ResLoginDto();
        if (merchant != null) {
            ResRegisterMerchantDto merchantDto = merchantMapper.toRegisterResponse(merchant, account);
            response.setMerchant(merchantDto);
        }
        response.setToken(token);

        return response;
    }
}
