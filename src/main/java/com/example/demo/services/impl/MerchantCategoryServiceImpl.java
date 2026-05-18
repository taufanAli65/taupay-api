package com.example.demo.services.impl;

import com.example.demo.config.CacheNames;
import com.example.demo.dtos.requests.ReqMerchantCategoryDto;
import com.example.demo.dtos.responses.ResMerchantCategoryDto;
import com.example.demo.entities.MerchantCategoryEntity;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.mappers.MerchantCategoryMapper;
import com.example.demo.repositories.MerchantCategoryRepository;
import com.example.demo.repositories.MerchantRepository;
import com.example.demo.services.MerchantCategoryService;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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
    private final MerchantRepository merchantRepository;

    @Override
    @Transactional
    @CachePut(cacheNames = CacheNames.MERCHANT_CATEGORY_BY_ID, key = "#result.id")
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
    @CacheEvict(cacheNames = CacheNames.MERCHANT_CATEGORY_BY_ID, key = "#id")
    public void deleteMerchantCategory(UUID id) {
        if (!merchantCategoryRepository.existsById(id)) {
            throw new DataNotFoundException("Merchant Category with ID: " + id + " not found");
        }

        if (merchantRepository.existsByCategoryId(id)) {
            throw new BadRequestException("Cannot delete category because it is still being used by merchants");
        }

        merchantCategoryRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CachePut(cacheNames = CacheNames.MERCHANT_CATEGORY_BY_ID, key = "#id")
    public ResMerchantCategoryDto updateMerchantCategoryName(UUID id, ReqMerchantCategoryDto req) {
        MerchantCategoryEntity merchantCategory = merchantCategoryRepository.findById(id).orElseThrow(
                () -> new DataNotFoundException("Merchant Category with ID: " + id + " not found")
        );
        merchantCategory.setName(req.getName());
        MerchantCategoryEntity updatedMerchantCategory = merchantCategoryRepository.save(merchantCategory);
        return merchantCategoryMapper.toResponse(updatedMerchantCategory);
    }

    @Override
    public List<ResMerchantCategoryDto> getAllMerchantCategories(String search) {
        List<MerchantCategoryEntity> categories;
        if (search != null && !search.isBlank()) {
            categories = merchantCategoryRepository.findByNameContainingIgnoreCaseOrderByNameAsc(search);
        } else {
            categories = merchantCategoryRepository.findAllByOrderByNameAsc();
        }

        return categories.stream()
                .map(merchantCategoryMapper::toResponse)
                .collect(Collectors.toList());
    }
}
