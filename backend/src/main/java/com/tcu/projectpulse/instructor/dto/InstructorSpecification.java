package com.tcu.projectpulse.instructor.dto;

import com.tcu.projectpulse.instructor.domain.Instructor;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class InstructorSpecification implements Specification<Instructor> {

    private final InstructorSearchCriteria criteria;

    public InstructorSpecification(InstructorSearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<Instructor> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (isSet(criteria.getFirstName())) {
            predicates.add(cb.like(cb.lower(root.get("firstName")), "%" + criteria.getFirstName().toLowerCase() + "%"));
        }
        if (isSet(criteria.getLastName())) {
            predicates.add(cb.like(cb.lower(root.get("lastName")), "%" + criteria.getLastName().toLowerCase() + "%"));
        }
        if (criteria.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
        }
        if (isSet(criteria.getTeamName())) {
            Join<Object, Object> teamJoin = root.join("teams", JoinType.LEFT);
            predicates.add(cb.like(cb.lower(teamJoin.get("name")), "%" + criteria.getTeamName().toLowerCase() + "%"));
            query.distinct(true);
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }

    private boolean isSet(String value) {
        return value != null && !value.isBlank();
    }
}
