package com.example.demo.services.impl;

import com.example.demo.dtos.requests.ReqLoginDto;
import com.example.demo.dtos.requests.ReqRegisterDto;
import com.example.demo.dtos.requests.ReqRegisterMerchantDto;
import com.example.demo.dtos.responses.ResLoginDto;
import com.example.demo.dtos.responses.ResRegisterDto;
import com.example.demo.dtos.responses.ResRegisterMerchantDto;
import com.example.demo.entities.*;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.exceptions.DuplicateResourceException;
import com.example.demo.mappers.AccountMapper;
import com.example.demo.mappers.MerchantMapper;
import com.example.demo.mappers.UserMapper;
import com.example.demo.repositories.*;
import com.example.demo.services.AuthService;
import com.example.demo.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantCategoryRepository merchantCategoryRepository;
    private final UserMapper userMapper;
    private final AccountMapper accountMapper;
    private final MerchantMapper merchantMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public ResLoginDto login(ReqLoginDto request) {
        AccountEntity account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new DataNotFoundException("Account not found"));

        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new BadRequestException("Email and password doesn't match");
        }

        String token;
        if (account.getRole() == RoleEnum.USER) {
            UserEntity user = userRepository.findByAccountId(account.getId())
                    .orElseThrow(() -> new DataNotFoundException("User profile not found"));
            token = jwtUtil.generateToken(account.getEmail(), account.getRole().name(), user.getId());
            return accountMapper.toLoginResponse(account, user, token);
        } else if (account.getRole() == RoleEnum.MERCHANT) {
            MerchantEntity merchant = merchantRepository.findByAccountId(account.getId())
                    .orElseThrow(() -> new DataNotFoundException("Merchant profile not found"));
            token = jwtUtil.generateToken(account.getEmail(), account.getRole().name(), merchant.getId());
            return accountMapper.toLoginResponse(account, merchant, token);
        } else {
            token = jwtUtil.generateToken(account.getEmail(), account.getRole().name(), account.getId());
            ResLoginDto response = new ResLoginDto();
            response.setToken(token);
            return response;
        }
    }

    @Override
    @Transactional
    public ResRegisterDto register(ReqRegisterDto request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already used");
        }
        UserEntity newUser = userMapper.toEntity(request);
        AccountEntity newAccount = accountMapper.toEntity(request);
        newAccount.setPassword(passwordEncoder.encode(request.getPassword()));

        AccountEntity savedAccount = accountRepository.save(newAccount);
        newUser.setAccount(savedAccount);
        UserEntity savedUser = userRepository.save(newUser);
        return userMapper.toRegisterResponse(savedUser, savedAccount);
    }

    @Override
    @Transactional
    public ResRegisterMerchantDto registerMerchant(ReqRegisterMerchantDto request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already used");
        }

        MerchantCategoryEntity category = merchantCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new DataNotFoundException("Merchant category not found"));

        AccountEntity newAccount = accountMapper.toEntity(request);
        newAccount.setPassword(passwordEncoder.encode(request.getPassword()));
        AccountEntity savedAccount = accountRepository.save(newAccount);

        MerchantEntity merchant = merchantMapper.toEntity(request, category);
        merchant.setAccount(savedAccount);
        MerchantEntity savedMerchant = merchantRepository.save(merchant);

        return merchantMapper.toRegisterResponse(savedMerchant, savedAccount);
    }
}
