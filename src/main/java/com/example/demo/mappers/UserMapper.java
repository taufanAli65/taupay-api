package com.example.demo.mappers;

import com.example.demo.dtos.requests.ReqRegisterDto;
import com.example.demo.dtos.responses.ResRegisterDto;
import com.example.demo.entities.AccountEntity;
import com.example.demo.entities.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserEntity toEntity(ReqRegisterDto dto) {
        if (dto == null) {
            return null;
        }

        UserEntity entity = new UserEntity();
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setAddress(dto.getAddress());
        entity.setBirthDate(dto.getBirthDate());
        entity.setIsActive(true);
        return entity;
    }

    public ResRegisterDto toRegisterResponse(UserEntity user, AccountEntity account) {
        ResRegisterDto response = new ResRegisterDto();
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setAddress(user.getAddress());
        response.setBirthOfDate(String.valueOf(user.getBirthDate()));

        if (account != null) {
            response.setEmail(account.getEmail());
        }

        return response;
    }
}
