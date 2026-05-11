package com.example.demo.services.impl;

import com.example.demo.dtos.requests.ReqCreateProductDto;
import com.example.demo.dtos.responses.ResCreateProductDto;
import com.example.demo.dtos.responses.ResProductDto;
import com.example.demo.entities.MerchantEntity;
import com.example.demo.entities.ProductEntity;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.mappers.ProductMapper;
import com.example.demo.repositories.MerchantRepository;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.services.FileService;
import com.example.demo.services.ProductService;
import com.example.demo.utils.PartialUpdateUtils;
import com.example.demo.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private static final long MAX_IMAGE_SIZE_BYTES = 2 * 1024 * 1024;

    private final ProductRepository productRepository;
    private final MerchantRepository merchantRepository;
    private final ProductMapper productMapper;
    private final FileService fileService;

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
    public ResCreateProductDto createProduct(ReqCreateProductDto request, MultipartFile file) {
        MerchantEntity merchant = getMerchantByProfile();
        
        ProductEntity product = productMapper.toEntity(request);
        product.setMerchant(merchant);

        if (file != null) {
            String imageName = uploadProductImage(file);
            product.setImageName(imageName);
        }

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
    public ResCreateProductDto updateProduct(UUID id, ReqCreateProductDto request, MultipartFile file) {
        MerchantEntity merchant = getMerchantByProfile();
        ProductEntity product = productRepository.findByIdAndMerchantIdAndIsActiveTrue(id, merchant.getId())
                .orElseThrow(() -> new DataNotFoundException("Product not found"));

        PartialUpdateUtils.copyNonNullProperties(request, product);

        String oldImageName = product.getImageName();
        if (file != null) {
            String newImageName = uploadProductImage(file);
            product.setImageName(newImageName);
        }

        ProductEntity savedProduct = productRepository.save(product);

        if (file != null && oldImageName != null) {
            deleteProductImage(oldImageName);
        }

        return productMapper.toCreateResponse(savedProduct);
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

    private String uploadProductImage(MultipartFile file) {
        validateImage(file);
        try {
            return fileService.uploadFile(file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload product image");
        }
    }

    private void deleteProductImage(String imageName) {
        try {
            fileService.deleteFile(imageName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete product image");
        }
    }

    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new BadRequestException("File size must be <= 2 MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("File must be an image");
        }
    }
}
