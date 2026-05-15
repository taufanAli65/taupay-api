package com.example.demo.repositories.specs;

import com.example.demo.dtos.requests.ReqMerchantFilterDto;
import com.example.demo.entities.MerchantEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class MerchantSpecification {

    public static Specification<MerchantEntity> filterBy(ReqMerchantFilterDto filterDto) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filterDto.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), filterDto.getIsActive()));
            }

            if (filterDto.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filterDto.getCategoryId()));
            }

            if (filterDto.getSearch() != null && !filterDto.getSearch().isBlank()) {
                String pattern = "%" + filterDto.getSearch().toLowerCase() + "%";
                
                // Implicit joins via get() to reuse EntityGraph joins
                Predicate nameLike = cb.like(cb.lower(root.get("name")), pattern);
                Predicate addressLike = cb.like(cb.lower(root.get("address")), pattern);
                Predicate emailLike = cb.like(cb.lower(root.get("account").get("email")), pattern);
                Predicate categoryNameLike = cb.like(cb.lower(root.get("category").get("name")), pattern);

                predicates.add(cb.or(nameLike, addressLike, emailLike, categoryNameLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
