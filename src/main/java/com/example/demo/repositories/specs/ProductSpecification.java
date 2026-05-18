package com.example.demo.repositories.specs;

import com.example.demo.dtos.requests.ReqProductFilterDto;
import com.example.demo.entities.MerchantEntity;
import com.example.demo.entities.ProductCategoryEntity;
import com.example.demo.entities.ProductEntity;
import com.example.demo.entities.ProductQuantityEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductSpecification {

    public static Specification<ProductEntity> filterBy(ReqProductFilterDto filterDto, UUID merchantId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<ProductEntity, ProductCategoryEntity> categoryJoin = root.join("category", JoinType.LEFT);
            Join<ProductEntity, ProductQuantityEntity> quantityJoin = root.join("quantityEntity", JoinType.LEFT);
            Join<ProductEntity, MerchantEntity> merchantJoin = root.join("merchant", JoinType.LEFT);

            if (merchantId != null) {
                predicates.add(cb.equal(merchantJoin.get("id"), merchantId));
            }

            if (filterDto.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), filterDto.getIsActive()));
            }

            if (filterDto.getCategoryId() != null) {
                predicates.add(cb.equal(categoryJoin.get("id"), filterDto.getCategoryId()));
            }

            if (filterDto.getInStock() != null) {
                if (filterDto.getInStock()) {
                    predicates.add(cb.greaterThan(quantityJoin.get("stock"), 0));
                } else {
                    predicates.add(cb.equal(quantityJoin.get("stock"), 0));
                }
            }

            if (filterDto.getSearch() != null && !filterDto.getSearch().isBlank()) {
                String pattern = "%" + filterDto.getSearch().toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("name")), pattern);
                Predicate priceLike = cb.like(cb.function("to_char", String.class, root.get("price"), cb.literal("FM999999999999999999")), pattern);
                Predicate stockLike = cb.like(cb.function("to_char", String.class, quantityJoin.get("stock"), cb.literal("FM999999999999999999")), pattern);
                Predicate categoryNameLike = cb.like(cb.lower(categoryJoin.get("name")), pattern);
                Predicate merchantNameLike = cb.like(cb.lower(merchantJoin.get("name")), pattern);

                predicates.add(cb.or(nameLike, priceLike, stockLike, categoryNameLike, merchantNameLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
