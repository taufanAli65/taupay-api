package com.example.demo.services.impl;

import com.example.demo.dtos.requests.ReqProductCategoryDto;
import com.example.demo.dtos.responses.ResProductCategoryDto;
import com.example.demo.entities.MerchantEntity;
import com.example.demo.entities.ProductCategoryEntity;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.mappers.ProductCategoryMapper;
import com.example.demo.repositories.MerchantRepository;
import com.example.demo.repositories.ProductCategoryRepository;
import com.example.demo.services.ProductCategoryService;
import com.example.demo.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductCategoryMapper productCategoryMapper;
    private final MerchantRepository merchantRepository;

    @Override
    public List<ResProductCategoryDto> getAllProductCategories() {
        MerchantEntity merchant = getMerchantByProfile();
        return productCategoryRepository.findAllByMerchantId(merchant.getId())
                .stream()
                .map(category -> productCategoryMapper.toProductCategoryResponse(category, category.getMerchant()))
                .toList();
    }

    @Override
    public ResProductCategoryDto getProductCategoryById(UUID id) {
        MerchantEntity merchant = getMerchantByProfile();

        ProductCategoryEntity category = productCategoryRepository.findByIdAndMerchantId(id, merchant.getId())
                .orElseThrow(() -> new DataNotFoundException("Category not found"));

        return productCategoryMapper.toProductCategoryResponse(category, merchant);
    }

    @Override
    public ResProductCategoryDto createProductCategory(ReqProductCategoryDto request) {
        MerchantEntity merchant = getMerchantByProfile();

        ProductCategoryEntity category = productCategoryMapper.toEntity(request);
        category.setMerchant(merchant);

        return productCategoryMapper.toProductCategoryResponse(productCategoryRepository.save(category), merchant);
    }

    @Override
    public ResProductCategoryDto updateProductCategory(UUID id, ReqProductCategoryDto request) {
        MerchantEntity merchant = getMerchantByProfile();
        ProductCategoryEntity category = productCategoryRepository.findByIdAndMerchantId(id, merchant.getId())
                .orElseThrow(() -> new DataNotFoundException("Category not found"));

        category.setName(request.getName());
        return productCategoryMapper.toProductCategoryResponse(productCategoryRepository.save(category), merchant);
    }

    @Override
    public void deleteProductCategory(UUID id) {
        MerchantEntity merchant = getMerchantByProfile();
        ProductCategoryEntity category = productCategoryRepository.findByIdAndMerchantId(id, merchant.getId())
                .orElseThrow(() -> new DataNotFoundException("Category not found"));

        productCategoryRepository.deleteById(category.getId());
    }

    private MerchantEntity getMerchantByProfile() {
        UUID merchantId = SecurityUtils.getCurrentProfileId();
        if (merchantId == null) {
            throw new UnauthorizedException("Token is invalid or expired");
        }

        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> new DataNotFoundException("Merchant Not Found"));
    }
}
