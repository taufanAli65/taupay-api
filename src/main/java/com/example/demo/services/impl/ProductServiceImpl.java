package com.example.demo.services.impl;

import com.example.demo.dtos.requests.ReqCreateProductDto;
import com.example.demo.dtos.responses.ResCreateProductDto;
import com.example.demo.dtos.responses.ResProductDto;
import com.example.demo.entities.MerchantEntity;
import com.example.demo.entities.ProductCategoryEntity;
import com.example.demo.entities.ProductEntity;
import com.example.demo.entities.ProductQuantityEntity;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.mappers.ProductMapper;
import com.example.demo.repositories.MerchantRepository;
import com.example.demo.repositories.ProductCategoryRepository;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.services.FileService;
import com.example.demo.services.ProductService;
import com.example.demo.utils.PartialUpdateUtils;
import com.example.demo.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private static final long MAX_IMAGE_SIZE_BYTES = 2 * 1024 * 1024;

    private final ProductRepository productRepository;
    private final MerchantRepository merchantRepository;
    private final ProductCategoryRepository productCategoryRepository;
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
        
        ProductCategoryEntity category = null;
        if (request.getCategoryId() != null) {
            category = productCategoryRepository.findByIdAndMerchantId(request.getCategoryId(), merchant.getId())
                    .orElseThrow(() -> new DataNotFoundException("Category not found"));
        }
        
        ProductEntity product = productMapper.toEntity(request);
        product.setMerchant(merchant);

        if (file != null) {
            String imageName = uploadProductImage(file);
            product.setImageName(imageName);
        }
        product.setCategory(category);

        ProductQuantityEntity quantity = new ProductQuantityEntity();
        quantity.setProduct(product);
        quantity.setStock(request.getStock());
        product.setQuantityEntity(quantity);

        return productMapper.toCreateResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public List<ResCreateProductDto> createBulkProducts(List<ReqCreateProductDto> requests) {
        MerchantEntity merchant = getMerchantByProfile();

        List<ProductEntity> products = requests.stream().map(request -> {
            ProductCategoryEntity category = null;
            if (request.getCategoryId() != null) {
                category = productCategoryRepository.findByIdAndMerchantId(request.getCategoryId(), merchant.getId())
                        .orElseThrow(() -> new DataNotFoundException("Category not found for product: " + request.getName()));
            }
            
            ProductEntity product = productMapper.toEntity(request);
            product.setMerchant(merchant);
            product.setCategory(category);

            ProductQuantityEntity quantity = new ProductQuantityEntity();
            quantity.setProduct(product);
            quantity.setStock(request.getStock() != null ? request.getStock() : 0);
            product.setQuantityEntity(quantity);

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
        if (request.getCategoryId() != null) {
            ProductCategoryEntity category = productCategoryRepository.findByIdAndMerchantId(request.getCategoryId(), merchant.getId())
                    .orElseThrow(() -> new DataNotFoundException("Category not found"));
            product.setCategory(category);
        }

        if (request.getStock() != null) {
            ProductQuantityEntity quantity = product.getQuantityEntity();
            if (quantity == null) {
                quantity = new ProductQuantityEntity();
                quantity.setProduct(product);
                product.setQuantityEntity(quantity);
            }
            quantity.setStock(request.getStock());
        }

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
            log.error("Error uploading product image: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload product image: " + e.getMessage(), e);
        }
    }

    private void deleteProductImage(String imageName) {
        try {
            fileService.deleteFile(imageName);
        } catch (Exception e) {
            log.error("Error deleting product image: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete product image: " + e.getMessage(), e);
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
