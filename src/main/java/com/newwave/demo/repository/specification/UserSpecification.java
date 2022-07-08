package com.newwave.demo.repository.specification;

import com.newwave.demo.models.UserModel;
import com.newwave.demo.payload.request.SearchUserRequest;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class UserSpecification extends SearchSpecification<SearchUserRequest, UserModel> {
    public UserSpecification(SearchUserRequest search) {
        super(search);
    }

    @Override
    public Predicate toPredicate(Root<UserModel> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder, SearchUserRequest search) {
        List<Predicate> predicates = new ArrayList<Predicate>();

        if (StringUtils.isNotEmpty(search.getUserName())) {
            Predicate likeFirstName = criteriaBuilder.like(root.get("username"), "%" + search.getUserName() + "%" );
            predicates.add(likeFirstName);
        }

        if (StringUtils.isNotEmpty(search.getEmail())) {
            Predicate likeLastName = criteriaBuilder.like(root.get("email"), "%" + search.getEmail() + "%" );
            predicates.add(likeLastName);
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
