package com.newwave.demo.repository;

import com.newwave.demo.models.UserModel;
import com.newwave.demo.payload.response.projection.ChartResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserModel, Long> , JpaSpecificationExecutor<UserModel> {
    Optional<UserModel> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    @Query(value = "SELECT * from (SELECT count(age) as 'youngAdult' FROM users s where s.age < 30 and s.age>0) a,\n" +
            "(SELECT count(age) as 'middleAged' FROM users s where s.age > 30 and s.age < 50) b,\n" +
            "(SELECT count(age) as 'old' FROM users s where s.age >= 50) c,\n" +
            "(SELECT count(age) as 'unknown' FROM users s where s.age =0) d;", nativeQuery = true)
    ChartResponse getChartForAge();
}
