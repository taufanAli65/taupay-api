package com.example.demo.mappers;

import com.example.demo.dtos.requests.ReqRegisterDto;
import com.example.demo.dtos.responses.ResRegisterDto;
import com.example.demo.dtos.responses.ResUserDto;
import com.example.demo.entities.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper extends BaseMapper<UserEntity, ReqRegisterDto, ResRegisterDto> {
    
    @Override
    public UserEntity toEntity(ReqRegisterDto dto) {
        if (dto == null) {
            return null;
        }

        UserEntity entity = new UserEntity();
        map(dto, entity);
        entity.setIsActive(true);
        return entity;
    }

    @Override
    public ResRegisterDto toResponse(UserEntity user) {
        if (user == null) {
            return null;
        }
        ResRegisterDto response = new ResRegisterDto();
        map(user, response);
        response.setBirthDate(String.valueOf(user.getBirthDate()));
        return response;
    }

    public ResUserDto toUserResponse(UserEntity user) {
        if (user == null) {
            return null;
        }
        ResUserDto response = new ResUserDto();
        map(user, response);
        if (user.getAccount() != null) {
            response.setEmail(user.getAccount().getEmail());
            response.setIsPinSet(user.getAccount().getPin() != null && !user.getAccount().getPin().isBlank());
        }
        return response;
    }

    public ResRegisterDto toRegisterResponse(UserEntity user) {
        ResRegisterDto response = toResponse(user);
        if (user.getAccount() != null && response != null) {
            response.setEmail(user.getAccount().getEmail());
        }
        return response;
    }
}
