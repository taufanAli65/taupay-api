package com.example.demo.seeders;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import com.example.demo.entities.MerchantCategoryEntity;
import com.example.demo.repositories.MerchantCategoryRepository;

@Slf4j
@Component
public class MerchantCategorySeeder implements ApplicationRunner {
    private final MerchantCategoryRepository merchantCategoryRepository;

    public MerchantCategorySeeder(MerchantCategoryRepository merchantCategoryRepository) {
        this.merchantCategoryRepository = merchantCategoryRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
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

            log.info("Merchant categories seeded successfully. Total: {}", categories.length);
        } else {
            log.info("Merchant categories already exist. Skipping seed.");
        }
    }
}
