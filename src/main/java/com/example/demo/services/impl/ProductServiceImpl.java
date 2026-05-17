package com.example.demo.services.impl;

import com.example.demo.dtos.requests.ReqCreateProductDto;
import com.example.demo.dtos.requests.ReqProductFilterDto;
import com.example.demo.dtos.responses.ResCreateProductDto;
import com.example.demo.dtos.responses.ResProductDto;
import com.example.demo.dtos.responses.ResProductStatisticsDto;
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
import com.example.demo.repositories.specs.ProductSpecification;
import com.example.demo.services.FileService;
import com.example.demo.services.ProductService;
import com.example.demo.utils.PartialUpdateUtils;
import com.example.demo.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
    public Page<ResProductDto> getAllProduct(ReqProductFilterDto filterDto, int page, int size) {
        PageRequest pageRequest;
        if (filterDto.getSortBy() != null && !filterDto.getSortBy().isBlank()) {
            Sort.Direction direction = (filterDto.getSortDir() != null && filterDto.getSortDir().equalsIgnoreCase("ASC")) 
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            pageRequest = PageRequest.of(page, size, Sort.by(direction, filterDto.getSortBy()));
        } else {
            pageRequest = PageRequest.of(page, size);
        }
        MerchantEntity merchant = getMerchantByProfile();

        filterDto.setIsActive(true);

        Specification<ProductEntity> spec = ProductSpecification.filterBy(filterDto, merchant.getId());
        return productRepository.findAll(spec, pageRequest).map(product -> productMapper.toProductResponse(product, product.getMerchant()));
    }

    @Override
    public Page<ResProductDto> findDeactivatedProducts(ReqProductFilterDto filterDto, int page, int size) {
        PageRequest pageRequest;
        if (filterDto.getSortBy() != null && !filterDto.getSortBy().isBlank()) {
            Sort.Direction direction = (filterDto.getSortDir() != null && filterDto.getSortDir().equalsIgnoreCase("ASC")) 
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            pageRequest = PageRequest.of(page, size, Sort.by(direction, filterDto.getSortBy()));
        } else {
            pageRequest = PageRequest.of(page, size);
        }
        MerchantEntity merchant = getMerchantByProfile();

        filterDto.setIsActive(false);

        Specification<ProductEntity> spec = ProductSpecification.filterBy(filterDto, merchant.getId());
        return productRepository.findAll(spec, pageRequest).map(product -> productMapper.toProductResponse(product, product.getMerchant()));
    }

    @Override
    public Page<ResProductDto> findAllProducts(ReqProductFilterDto filterDto) {
        int size = filterDto.getSize() != null ? filterDto.getSize() : 10;
        int page = filterDto.getPage() != null ? filterDto.getPage() : 0;
        PageRequest pageRequest;
        if (filterDto.getSortBy() != null && !filterDto.getSortBy().isBlank()) {
            Sort.Direction direction = (filterDto.getSortDir() != null && filterDto.getSortDir().equalsIgnoreCase("ASC")) 
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            pageRequest = PageRequest.of(page, size, Sort.by(direction, filterDto.getSortBy()));
        } else {
            pageRequest = PageRequest.of(page, size);
        }

        Specification<ProductEntity> spec = ProductSpecification.filterBy(filterDto, null);
        return productRepository.findAll(spec, pageRequest).map(product -> productMapper.toProductResponse(product, product.getMerchant()));
    }

    @Override
    public Page<ResProductDto> getProductsByMerchantId(UUID merchantId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductEntity> products = productRepository.findAllByMerchantId(merchantId, pageable);
        return products.map(productMapper::toResponse);
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
    public ResProductStatisticsDto getProductStatistics() {
        MerchantEntity merchant = getMerchantByProfile();
        UUID merchantId = merchant.getId();

        return ResProductStatisticsDto.builder()
                .totalProducts(productRepository.countByMerchantId(merchantId))
                .activeProducts(productRepository.countByMerchantIdAndIsActiveTrue(merchantId))
                .deactivatedProducts(productRepository.countByMerchantIdAndIsActiveFalse(merchantId))
                .build();
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

        if (Boolean.TRUE.equals(request.getIsImageRemoved())) {
            product.setImageName(null);
            if (oldImageName != null) {
                deleteProductImage(oldImageName);
            }
        } else if (file != null && !file.isEmpty()) {
            String newImageName = uploadProductImage(file);
            product.setImageName(newImageName);
            if (oldImageName != null) {
                deleteProductImage(oldImageName);
            }
        }
        ProductEntity savedProduct = productRepository.save(product);
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
            return fileService.uploadFile(file, "products");
        } catch (Exception e) {
            log.error("Error uploading product image: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload product image: " + e.getMessage(), e);
        }
    }

    private void deleteProductImage(String imageName) {
        try {
            fileService.deleteFile("products", imageName);
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
