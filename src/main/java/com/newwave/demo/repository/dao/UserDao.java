package com.newwave.demo.repository.dao;

import com.newwave.demo.models.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserDao {

    Page<UserModel> getUser(Pageable pageable);
}
