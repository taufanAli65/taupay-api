package com.example.demo.services.impl;

import com.example.demo.dtos.requests.ReqCreateProductDto;
import com.example.demo.dtos.responses.ResCreateProductDto;
import com.example.demo.dtos.responses.ResProductDto;
import com.example.demo.entities.MerchantEntity;
import com.example.demo.entities.ProductEntity;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.mappers.ProductMapper;
import com.example.demo.repositories.MerchantRepository;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.services.ProductService;
import com.example.demo.utils.PartialUpdateUtils;
import com.example.demo.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final MerchantRepository merchantRepository;
    private final ProductMapper productMapper;

    @Override
    public Page<ResProductDto> getAllProduct(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductEntity> products;

        if (SecurityUtils.hasRole("SUPER_ADMIN")) {
            products = productRepository.findAllByIsActiveTrue(pageable);
        } else {
            MerchantEntity merchant = getMerchantByProfile();
            products = productRepository.findAllByMerchantIdAndIsActiveTrue(merchant.getId(), pageable);
        }
        
        return products.map(product -> productMapper.toProductResponse(product, product.getMerchant()));
    }

    @Override
    public ResProductDto getProductById(UUID id) {
        ProductEntity product;

        if (SecurityUtils.hasRole("SUPER_ADMIN")) {
            product = productRepository.findByIdAndIsActiveTrue(id)
                    .orElseThrow(() -> new DataNotFoundException("Product not found"));
        } else {
            MerchantEntity merchant = getMerchantByProfile();
            product = productRepository.findByIdAndMerchantIdAndIsActiveTrue(id, merchant.getId())
                    .orElseThrow(() -> new DataNotFoundException("Product not found"));
        }

        return productMapper.toProductResponse(product, product.getMerchant());
    }

    @Override
    @Transactional
    public ResCreateProductDto createProduct(ReqCreateProductDto request) {
        MerchantEntity merchant = getMerchantByProfile();
        
        ProductEntity product = productMapper.toEntity(request);
        product.setMerchant(merchant);

        return productMapper.toCreateResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public List<ResCreateProductDto> createBulkProducts(List<ReqCreateProductDto> requests) {
        MerchantEntity merchant = getMerchantByProfile();

        List<ProductEntity> products = requests.stream().map(request -> {
            ProductEntity product = productMapper.toEntity(request);
            product.setMerchant(merchant);
            return product;
        }).toList();

        return productRepository.saveAll(products).stream()
                .map(productMapper::toCreateResponse)
                .toList();
    }

    @Override
    @Transactional
    public ResCreateProductDto updateProduct(UUID id, ReqCreateProductDto request) {
        MerchantEntity merchant = getMerchantByProfile();
        ProductEntity product = productRepository.findByIdAndMerchantIdAndIsActiveTrue(id, merchant.getId())
                .orElseThrow(() -> new DataNotFoundException("Product not found"));

        PartialUpdateUtils.copyNonNullProperties(request, product);
        return productMapper.toCreateResponse(productRepository.save(product));
    }

    @Override
    public void deactivateProduct(UUID id) {
        ProductEntity product;
        if (SecurityUtils.hasRole("SUPER_ADMIN")) {
            product = productRepository.findByIdAndIsActiveTrue(id)
                    .orElseThrow(() -> new DataNotFoundException("Product not found"));
        } else {
            MerchantEntity merchant = getMerchantByProfile();
            product = productRepository.findByIdAndMerchantIdAndIsActiveTrue(id, merchant.getId())
                    .orElseThrow(() -> new DataNotFoundException("Product not found"));
        }

        product.setIsActive(false);
        productRepository.save(product);
    }

    @Override
    public void activateProduct(UUID id) {
        ProductEntity product;
        if (SecurityUtils.hasRole("SUPER_ADMIN")) {
            product = productRepository.findById(id)
                    .orElseThrow(() -> new DataNotFoundException("Product not found"));
        } else {
            MerchantEntity merchant = getMerchantByProfile();
            product = productRepository.findByIdAndMerchantId(id, merchant.getId())
                    .orElseThrow(() -> new DataNotFoundException("Product not found"));
        }

        product.setIsActive(true);
        productRepository.save(product);
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
