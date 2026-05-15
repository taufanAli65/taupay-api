package com.example.demo.services.impl;

import com.example.demo.config.CacheNames;
import com.example.demo.dtos.requests.ReqMerchantCategoryDto;
import com.example.demo.dtos.responses.ResMerchantCategoryDto;
import com.example.demo.entities.MerchantCategoryEntity;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.mappers.MerchantCategoryMapper;
import com.example.demo.repositories.MerchantCategoryRepository;
import com.example.demo.services.MerchantCategoryService;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MerchantCategoryServiceImpl implements MerchantCategoryService {
    private final MerchantCategoryRepository merchantCategoryRepository;
    private final MerchantCategoryMapper merchantCategoryMapper;

    @Override
    @Transactional
    @Caching(
            put = @CachePut(cacheNames = CacheNames.MERCHANT_CATEGORY_BY_ID, key = "#result.id"),
            evict = @CacheEvict(cacheNames = CacheNames.MERCHANT_CATEGORY_LIST, key = "'all'")
    )
    public ResMerchantCategoryDto createMerchantCategory(ReqMerchantCategoryDto req) {
        MerchantCategoryEntity merchantCategory = new MerchantCategoryEntity();
        merchantCategory.setName(req.getName());
        MerchantCategoryEntity savedMerchantCategory = merchantCategoryRepository.save(merchantCategory);
        return merchantCategoryMapper.toResponse(savedMerchantCategory);
    }

    @Override
    @Cacheable(cacheNames = CacheNames.MERCHANT_CATEGORY_BY_ID, key = "#id")
    public ResMerchantCategoryDto getMerchantCategoryById(UUID id) {
        MerchantCategoryEntity merchantCategory = merchantCategoryRepository.findById(id).orElseThrow(
                () -> new DataNotFoundException("Merchant Category with ID: " + id + " not found")
        );
        return merchantCategoryMapper.toResponse(merchantCategory);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.MERCHANT_CATEGORY_BY_ID, key = "#id"),
            @CacheEvict(cacheNames = CacheNames.MERCHANT_CATEGORY_LIST, key = "'all'")
    })
    public void deleteMerchantCategory(UUID id) {
        if (!merchantCategoryRepository.existsById(id)) {
            throw new DataNotFoundException("Merchant Category with ID: " + id + " not found");
        }
        merchantCategoryRepository.deleteById(id);
    }

    @Override
    @Transactional
    @Caching(
            put = @CachePut(cacheNames = CacheNames.MERCHANT_CATEGORY_BY_ID, key = "#id"),
            evict = @CacheEvict(cacheNames = CacheNames.MERCHANT_CATEGORY_LIST, key = "'all'")
    )
    public ResMerchantCategoryDto updateMerchantCategoryName(UUID id, ReqMerchantCategoryDto req) {
        MerchantCategoryEntity merchantCategory = merchantCategoryRepository.findById(id).orElseThrow(
                () -> new DataNotFoundException("Merchant Category with ID: " + id + " not found")
        );
        merchantCategory.setName(req.getName());
        MerchantCategoryEntity updatedMerchantCategory = merchantCategoryRepository.save(merchantCategory);
        return merchantCategoryMapper.toResponse(updatedMerchantCategory);
    }

    @Override
    @Cacheable(cacheNames = CacheNames.MERCHANT_CATEGORY_LIST, key = "'all'")
    public List<ResMerchantCategoryDto> getAllMerchantCategories() {
        return merchantCategoryRepository.findAll()
                .stream()
                .map(merchantCategoryMapper::toResponse)
                .collect(Collectors.toList());
    }
}
