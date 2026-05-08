package com.example.demo.seeders;

import com.example.demo.entities.AccountEntity;
import com.example.demo.entities.RoleEnum;
import com.example.demo.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!accountRepository.existsByEmail("admin@taupay.com")) {
            AccountEntity admin = new AccountEntity();
            admin.setEmail("admin@taupay.com");
            admin.setPassword(passwordEncoder.encode("12345678"));
            admin.setRole(RoleEnum.SUPER_ADMIN);
            accountRepository.save(admin);
        }
    }
}
