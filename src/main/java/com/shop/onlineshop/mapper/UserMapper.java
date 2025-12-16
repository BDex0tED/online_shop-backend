package com.shop.onlineshop.mapper;

import com.shop.onlineshop.models.dto.UserDTO;
import com.shop.onlineshop.models.entity.UserEntity;
import com.shop.onlineshop.models.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "fullName", target = "fullName")
    UserResponse toUserResponse(UserEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "username", expression = "java(dto.getEmail())")
    UserEntity toEntity(UserDTO dto);
}
