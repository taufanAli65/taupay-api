package com.example.demo.mappers;

import com.example.demo.dtos.requests.ReqRegisterDto;
import com.example.demo.dtos.responses.ResLoginDto;
import com.example.demo.dtos.responses.ResRegisterDto;
import com.example.demo.entities.AccountEntity;
import com.example.demo.entities.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountMapper {
    private final UserMapper userMapper;

    public AccountEntity toEntity(ReqRegisterDto dto) {
        if (dto == null) {
            return null;
        }

        AccountEntity entity = new AccountEntity();
        entity.setEmail(dto.getEmail());
        entity.setRole("user");
        return entity;
    }

    public ResLoginDto toLoginResponse(AccountEntity account, UserEntity user, String token) {
        ResLoginDto response = new ResLoginDto();
        ResRegisterDto userDto = userMapper.toRegisterResponse(user, account);
        if (account != null) {
            response.setUser(userDto);
        }
        response.setToken(token);

        return response;
    }
}
