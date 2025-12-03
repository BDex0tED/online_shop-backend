package com.shop.onlineshop.dataservices.impl;

import com.shop.onlineshop.dataservices.RoleDataService;
import com.shop.onlineshop.models.entity.Role;
import com.shop.onlineshop.repo.RoleRepo;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class RoleDataServiceImpl implements RoleDataService {
  private final RoleRepo roleRepo;

  public RoleDataServiceImpl(RoleRepo roleRepo) {
    this.roleRepo = roleRepo;
  }

  @Override
  public Role findByName(String name) {
    if(name == null) throw new IllegalArgumentException("Role name cannot be null");
    return roleRepo.findByName(name).orElseThrow(()-> new EntityNotFoundException("Role not found"));
  }
}
