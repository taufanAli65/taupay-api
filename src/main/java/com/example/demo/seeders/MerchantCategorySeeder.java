package com.example.demo.seeders;

import com.example.demo.entities.MerchantCategoryEntity;
import com.example.demo.repositories.MerchantCategoryRepository;
import com.example.demo.services.MerchantCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MerchantCategorySeeder implements ApplicationRunner {
    private final MerchantCategoryRepository merchantCategoryRepository;
    private final MerchantCategoryService merchantCategoryService;

    public MerchantCategorySeeder(
            MerchantCategoryRepository merchantCategoryRepository,
            MerchantCategoryService merchantCategoryService
    ) {
        this.merchantCategoryRepository = merchantCategoryRepository;
        this.merchantCategoryService = merchantCategoryService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (merchantCategoryRepository.count() == 0) {
            log.info("Seeding merchant categories...");

            String[] categories = {
                    "Food & Beverage",
                    "Groceries",
                    "Retail",
                    "Healthcare",
                    "Beauty & Wellness",
                    "Transportation",
                    "Automotive",
                    "Entertainment",
                    "Accommodation",
                    "Fashion",
                    "Electronics",
                    "Home & Garden",
                    "Sports & Fitness",
                    "Education",
                    "Financial Services",
                    "Professional Services",
                    "Others"
            };

            for (String categoryName : categories) {
                MerchantCategoryEntity category = new MerchantCategoryEntity();
                category.setName(categoryName);
                merchantCategoryRepository.save(category);
            }

            merchantCategoryService.getAllMerchantCategories(null);
            log.info("Merchant categories seeded successfully. Total: {}", categories.length);
        } else {
            log.info("Merchant categories already exist. Skipping seed.");
        }
    }
}
