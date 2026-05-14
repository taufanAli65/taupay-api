package com.example.demo.repositories.specs;

import com.example.demo.dtos.requests.ReqUserFilterDto;
import com.example.demo.entities.UserEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<UserEntity> filterBy(ReqUserFilterDto filterDto) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filterDto.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), filterDto.getIsActive()));
            }

            if (filterDto.getSearch() != null && !filterDto.getSearch().isBlank()) {
                String pattern = "%" + filterDto.getSearch().toLowerCase() + "%";
                Predicate firstNameLike = cb.like(cb.lower(root.get("firstName")), pattern);
                Predicate lastNameLike = cb.like(cb.lower(root.get("lastName")), pattern);
                Predicate addressLike = cb.like(cb.lower(root.get("address")), pattern);
                Predicate emailLike = cb.like(cb.lower(root.get("account").get("email")), pattern);

                predicates.add(cb.or(firstNameLike, lastNameLike, addressLike, emailLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
