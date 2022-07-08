package com.newwave.demo.repository;

import com.newwave.demo.models.ERole;
import com.newwave.demo.models.RoleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


public interface RoleRepository extends JpaRepository<RoleModel, Long> {
    Optional<RoleModel> findByName(ERole name);
}
