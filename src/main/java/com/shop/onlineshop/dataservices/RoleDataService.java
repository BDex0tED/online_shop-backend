package com.shop.onlineshop.dataservices;

import com.shop.onlineshop.models.entity.Role;

public interface RoleDataService {
  Role findByName(String name);


}
