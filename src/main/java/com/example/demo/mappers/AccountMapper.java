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
public class AccountMapper extends BaseMapper<AccountEntity, Object, Object> {
    private final UserMapper userMapper;
    private final MerchantMapper merchantMapper;

    @Override
    public AccountEntity toEntity(Object dto) {
        if (dto == null) return null;
        AccountEntity entity = new AccountEntity();
        map(dto, entity);
        return entity;
    }

    @Override
    public Object toResponse(AccountEntity entity) {
        return null; // Not implemented
    }

    public AccountEntity toEntity(ReqRegisterDto dto) {
        AccountEntity entity = toEntity((Object) dto);
        if (entity != null) entity.setRole(RoleEnum.USER);
        return entity;
    }

    public AccountEntity toEntity(ReqRegisterMerchantDto dto) {
        AccountEntity entity = toEntity((Object) dto);
        if (entity != null) entity.setRole(RoleEnum.MERCHANT);
        return entity;
    }

    public ResLoginDto toLoginResponse(AccountEntity account, UserEntity user, String token) {
        ResLoginDto response = new ResLoginDto();
        if (user != null) {
            ResRegisterDto userDto = userMapper.toRegisterResponse(user);
            response.setUser(userDto);
        }
        response.setToken(token);

        return response;
    }

    public ResLoginDto toLoginResponse(MerchantEntity merchant, String token) {
        ResLoginDto response = new ResLoginDto();
        if (merchant != null) {
            ResRegisterMerchantDto merchantDto = merchantMapper.toRegisterResponse(merchant);
            response.setMerchant(merchantDto);
        }
        response.setToken(token);

        return response;
    }
}
