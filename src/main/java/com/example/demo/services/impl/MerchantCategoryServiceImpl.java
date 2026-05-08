package com.example.demo.services.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import com.example.demo.dtos.requests.ReqMerchantCategoryDto;
import com.example.demo.dtos.responses.ResMerchantCategoryDto;
import com.example.demo.repositories.MerchantCategoryRepository;
import com.example.demo.services.MerchantCategoryService;
import com.example.demo.entities.MerchantCategoryEntity;
import com.example.demo.exceptions.DataNotFoundException;

@Slf4j
@Service
public class MerchantCategoryServiceImpl implements MerchantCategoryService {
    private final MerchantCategoryRepository merchantCategoryRepository;

    public MerchantCategoryServiceImpl(MerchantCategoryRepository merchantCategoryRepository) {
        this.merchantCategoryRepository = merchantCategoryRepository;
    }

    @Override
    public ResMerchantCategoryDto createMerchantCategory(ReqMerchantCategoryDto req) {
        MerchantCategoryEntity merchantCategory = new MerchantCategoryEntity();
        merchantCategory.setName(req.getName());
        MerchantCategoryEntity savedMerchantCategory = merchantCategoryRepository.save(merchantCategory);
        return new ResMerchantCategoryDto(savedMerchantCategory.getId(), savedMerchantCategory.getName());
        // TODO: SAVE IT TO REDIS FOR CACHING PURPOSE
    }

    @Override
    public ResMerchantCategoryDto getMerchantCategoryById(UUID id) {
        // TODO: CHECK IF THE DATA IS AVAILABLE IN REDIS CACHE, IF NOT THEN GET IT FROM DATABASE AND SAVE IT TO REDIS
        MerchantCategoryEntity merchantCategory = merchantCategoryRepository.findById(id).orElseThrow(
                () -> new DataNotFoundException("Merchant Category with ID: " + id + " not found")
        );
        return new ResMerchantCategoryDto(merchantCategory.getId(), merchantCategory.getName());
    }

    @Override
    public void deleteMerchantCategory(UUID id) {
        // TODO: REMOVE THE DATA FROM REDIS CACHE
        if (!merchantCategoryRepository.existsById(id)) {
            throw new DataNotFoundException("Merchant Category with ID: " + id + " not found");
        }
        merchantCategoryRepository.deleteById(id);
    }

    @Override
    public ResMerchantCategoryDto updateMerchantCategoryName(UUID id, ReqMerchantCategoryDto req) {
        MerchantCategoryEntity merchantCategory = merchantCategoryRepository.findById(id).orElseThrow(
                () -> new DataNotFoundException("Merchant Category with ID: " + id + " not found")
        );
        merchantCategory.setName(req.getName());
        MerchantCategoryEntity updatedMerchantCategory = merchantCategoryRepository.save(merchantCategory);
        // TODO: UPDATE THE DATA IN REDIS CACHE
        return new ResMerchantCategoryDto(updatedMerchantCategory.getId(), updatedMerchantCategory.getName());
    }

    @Override
    public List<ResMerchantCategoryDto> getAllMerchantCategories() {
        // TODO: CHECK IF THE DATA IS AVAILABLE IN REDIS CACHE, IF NOT THEN GET IT FROM DATABASE AND SAVE IT TO REDIS
        return merchantCategoryRepository.findAll()
            .stream()
            .map(merchantCategory -> new ResMerchantCategoryDto(
                merchantCategory.getId(),
                merchantCategory.getName()
            ))
            .collect(Collectors.toList());
        }
    }
