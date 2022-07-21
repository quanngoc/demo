package com.newwave.demo.repository.dao.impl;

import com.newwave.demo.models.UserModel;
import com.newwave.demo.repository.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.List;


@Repository
public class UserDaoImpl implements UserDao {
    @Autowired
    private EntityManager entityManager;

    @Override
    public Page<UserModel> getUser(Pageable pageable) {
        StringBuilder sql = new StringBuilder("SELECT * FROM users ");
        Query query = entityManager.createNativeQuery(sql.toString(), UserModel.class);
        int offset = pageable.getPageNumber() * pageable.getPageSize();
        if (pageable.getPageNumber() > 0) {
            offset = offset - 5;
        }
        query.setFirstResult(offset);
        query.setMaxResults(pageable.getPageSize());

        List<UserModel> resultList = query.getResultList();
        Number totalRecord = getTotalElements(sql.toString());
        return new PageImpl<>(resultList, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), (Long) totalRecord);
    }

    protected long getTotalElements(String sql) {
        String countQuery = "SELECT count(*) FROM (" + sql + ") as CQ";
        Query query = entityManager.createNativeQuery(countQuery);
        try {
            Object object = query.getSingleResult();
            return ((BigInteger) object).longValue();
        } catch (NoResultException | ClassCastException e) {
            return 0;
        }
    }
}
