package com.example.demo.mappers;

import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Base generic mapper class providing automatic field copying using BeanUtils.
 * 
 * @param <E> Entity type
 * @param <RQ> Request DTO type
 * @param <RS> Response DTO type
 */
public abstract class BaseMapper<E, RQ, RS> {

    /**
     * Automatically copies properties from source to target where names match.
     */
    public void map(Object source, Object target) {
        if (source != null && target != null) {
            BeanUtils.copyProperties(source, target);
        }
    }

    public abstract E toEntity(RQ dto);

    public abstract RS toResponse(E entity);

    public List<E> toEntityList(List<RQ> dtos) {
        if (dtos == null) return null;
        return dtos.stream().map(this::toEntity).collect(Collectors.toList());
    }

    public List<RS> toResponseList(List<E> entities) {
        if (entities == null) return null;
        return entities.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
