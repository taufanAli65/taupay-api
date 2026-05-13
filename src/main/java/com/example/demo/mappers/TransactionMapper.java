package com.example.demo.mappers;

import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.dtos.responses.ResTransactionHistoryDto;
import org.springframework.stereotype.Component;

import com.example.demo.dtos.responses.ResTransactionDto;
import com.example.demo.entities.AccountProductTransactionEntity;
import com.example.demo.entities.AccountTransactionEntity;
import com.example.demo.entities.MerchantEntity;
import com.example.demo.entities.ProductEntity;
import com.example.demo.entities.ProductTransactionEntity;
import com.example.demo.entities.UserEntity;

@Component
public class TransactionMapper extends BaseMapper<AccountTransactionEntity, Object, ResTransactionDto> {
    
    @Override
    public AccountTransactionEntity toEntity(Object dto) {
        return null; // Not implemented as transaction creation uses multiple inputs
    }

    @Override
    public ResTransactionDto toResponse(AccountTransactionEntity entity) {
        if (entity == null) return null;
        ResTransactionDto response = new ResTransactionDto();
        response.setTrxId(entity.getId() != null ? entity.getId().toString() : null);
        if (entity.getReceiver() != null) {
            response.setMerchantId(entity.getReceiver().getId().toString());
        }
        response.setCreatedAt(entity.getCreatedAt());
        response.setTotal(entity.getAmount());
        return response;
    }

    public ResTransactionDto.ProductItem toProductItem(ProductEntity product, Integer quantity) {
        ResTransactionDto.ProductItem item = new ResTransactionDto.ProductItem();
        item.setProductId(product.getId().toString());
        item.setName(product.getName());
        item.setQuantity(quantity);
        item.setPrice(product.getPrice());
        return item;
    }

    public ResTransactionDto toResponse(
            String trxId,
            MerchantEntity merchant,
            LocalDateTime createdAt,
            List<ResTransactionDto.ProductItem> products,
            Long total
    ) {
        ResTransactionDto response = new ResTransactionDto();
        response.setTrxId(trxId);
        response.setMerchantId(merchant.getId().toString());
        response.setCreatedAt(createdAt);
        response.setProducts(products);
        response.setTotal(total);
        return response;
    }

    public AccountTransactionEntity toAccountTransaction(
            MerchantEntity merchant,
            UserEntity payer,
            Long amount,
            String category
    ) {
        AccountTransactionEntity accountTransaction = new AccountTransactionEntity();
        accountTransaction.setReceiver(merchant);
        accountTransaction.setRequester(payer);
        accountTransaction.setAmount(amount);
        accountTransaction.setIsSuccess(true);
        accountTransaction.setCategory(category);
        return accountTransaction;
    }

    public ProductTransactionEntity toProductTransaction(
            ProductEntity product,
            ResTransactionDto.ProductItem item,
            Long productPrice
    ) {
        ProductTransactionEntity productTransaction = new ProductTransactionEntity();
        productTransaction.setProduct(product);
        productTransaction.setProductName(item.getName());
        productTransaction.setProductPrice(productPrice);
        productTransaction.setQuantity(item.getQuantity());
        return productTransaction;
    }

    public AccountProductTransactionEntity toAccountProductTransaction(
            AccountTransactionEntity accountTransaction,
            ProductTransactionEntity productTransaction
    ) {
        AccountProductTransactionEntity link = new AccountProductTransactionEntity();
        link.setAccountTransaction(accountTransaction);
        link.setProductTransaction(productTransaction);
        return link;
    }

    public ResTransactionHistoryDto toHistoryResponse(AccountTransactionEntity entity, boolean isMerchantPerspective) {
        if (entity == null) return null;

        ResTransactionHistoryDto dto = new ResTransactionHistoryDto();
        dto.setHistoryId(entity.getId());
        dto.setAmount(entity.getAmount());
        dto.setCategory(entity.getCategory());
        dto.setCreatedAt(entity.getCreatedAt());

        // Determine counterparty name
        if (isMerchantPerspective) {
            dto.setCounterpartyName(entity.getRequester() != null ? 
                entity.getRequester().getFirstName() + " " + entity.getRequester().getLastName() : "Unknown User");
        } else {
            dto.setCounterpartyName(entity.getReceiver() != null ? 
                entity.getReceiver().getName() : "Unknown Merchant");
        }

        // Map product details
        if (entity.getItems() != null) {
            List<ResTransactionHistoryDto.ProductDetail> productDetails = entity.getItems().stream()
                .map(item -> {
                    ProductTransactionEntity pt = item.getProductTransaction();
                    return new ResTransactionHistoryDto.ProductDetail(
                        pt.getProductName(),
                        pt.getQuantity(),
                        pt.getProductPrice()
                    );
                }).toList();
            dto.setProducts(productDetails);
        }

        return dto;
    }
}
