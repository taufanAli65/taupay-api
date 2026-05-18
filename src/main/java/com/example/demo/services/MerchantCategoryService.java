package com.example.demo.services;

import com.example.demo.dtos.requests.ReqMerchantCategoryDto;
import com.example.demo.dtos.responses.ResMerchantCategoryDto;
import java.util.List;
import java.util.UUID;

public interface MerchantCategoryService {
    ResMerchantCategoryDto createMerchantCategory(ReqMerchantCategoryDto req);
    ResMerchantCategoryDto getMerchantCategoryById(UUID id);
    List<ResMerchantCategoryDto> getAllMerchantCategories(String search);
    ResMerchantCategoryDto updateMerchantCategoryName(UUID id, ReqMerchantCategoryDto req);
    void deleteMerchantCategory(UUID id);
}
