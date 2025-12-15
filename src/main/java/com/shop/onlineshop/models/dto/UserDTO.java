package com.shop.onlineshop.models.dto;

import lombok.Data;

@Data
public class UserDTO {
  private String username;
  private String email;
  private String password;
  private String confirmPassword;
}
