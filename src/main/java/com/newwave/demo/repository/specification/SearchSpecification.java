package com.newwave.demo.repository.specification;

import lombok.Data;
import org.springframework.data.jpa.domain.Specification;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@Data
public abstract class SearchSpecification<S,T> implements Specification<T> {
    private static final long serialVersionUID = 1L;

    private S search;

    public SearchSpecification(S search) {
        this.search = search;
    }

    public abstract Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder, S search);

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        return this.toPredicate(root, query, criteriaBuilder, this.search);
    }
}
