package com.example.demo.mappers;

import java.time.LocalDateTime;
import java.util.List;

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
}
