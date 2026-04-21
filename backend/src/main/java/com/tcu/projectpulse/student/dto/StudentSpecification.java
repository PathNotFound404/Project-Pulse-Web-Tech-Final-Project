package com.tcu.projectpulse.student.dto;

import com.tcu.projectpulse.student.domain.Student;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class StudentSpecification implements Specification<Student> {

    private final StudentSearchCriteria criteria;

    public StudentSpecification(StudentSearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<Student> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (isSet(criteria.getFirstName())) {
            predicates.add(cb.like(cb.lower(root.get("firstName")), "%" + criteria.getFirstName().toLowerCase() + "%"));
        }
        if (isSet(criteria.getLastName())) {
            predicates.add(cb.like(cb.lower(root.get("lastName")), "%" + criteria.getLastName().toLowerCase() + "%"));
        }
        if (isSet(criteria.getEmail())) {
            predicates.add(cb.like(cb.lower(root.get("email")), "%" + criteria.getEmail().toLowerCase() + "%"));
        }
        if (criteria.getSectionId() != null) {
            predicates.add(cb.equal(root.get("section").get("id"), criteria.getSectionId()));
        }
        if (isSet(criteria.getSectionName())) {
            predicates.add(cb.like(cb.lower(root.get("section").get("name")), "%" + criteria.getSectionName().toLowerCase() + "%"));
        }
        if (criteria.getTeamId() != null || isSet(criteria.getTeamName())) {
            Join<Object, Object> teamJoin = root.join("teams", JoinType.LEFT);
            if (criteria.getTeamId() != null) {
                predicates.add(cb.equal(teamJoin.get("id"), criteria.getTeamId()));
            }
            if (isSet(criteria.getTeamName())) {
                predicates.add(cb.like(cb.lower(teamJoin.get("name")), "%" + criteria.getTeamName().toLowerCase() + "%"));
            }
            query.distinct(true);
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }

    private boolean isSet(String value) {
        return value != null && !value.isBlank();
    }
}
