package com.shop.onlineshop.dataservices;

import com.shop.onlineshop.models.entity.UserEntity;

public interface UserEntityDataService {
  UserEntity getUserEntityByEmailOrThrow(String email);
  UserEntity getUserEntityByUsernameOrThrow(String username);
  UserEntity getUserEntityByIdOrThrow(Long id);
  void saveUserEntity(UserEntity userEntity);
  void updateUserEntity(UserEntity userEntity);
  boolean existsByEmail(String email);
  boolean existsByUsername(String username);
}
