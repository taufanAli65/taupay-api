package com.example.demo.services.impl;

import com.example.demo.dtos.requests.ReqLoginDto;
import com.example.demo.dtos.requests.ReqRegisterDto;
import com.example.demo.dtos.requests.ReqRegisterMerchantDto;
import com.example.demo.dtos.responses.ResLoginDto;
import com.example.demo.dtos.responses.ResMerchantDto;
import com.example.demo.dtos.responses.ResRegisterDto;
import com.example.demo.dtos.responses.ResRegisterMerchantDto;
import com.example.demo.entities.AccountEntity;
import com.example.demo.entities.MerchantEntity;
import com.example.demo.entities.OwnerTypeEnum;
import com.example.demo.entities.RoleEnum;
import com.example.demo.entities.UserEntity;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.exceptions.DuplicateResourceException;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.mappers.AccountMapper;
import com.example.demo.mappers.MerchantMapper;
import com.example.demo.mappers.UserMapper;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.AuthService;
import com.example.demo.services.MerchantService;
import com.example.demo.services.WalletService;
import com.example.demo.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final MerchantService merchantService;
    private final WalletService walletService;
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

        if (account.getRole() == RoleEnum.USER) {
            UserEntity user = account.getUser();
            if (user == null) {
                throw new DataNotFoundException("User profile not found");
            }
            String token = jwtUtil.generateToken(account.getEmail(), account.getRole().name(), user.getId());
            return accountMapper.toLoginResponse(account, user, token);
        }

        if (account.getRole() == RoleEnum.MERCHANT) {
            MerchantEntity merchant = account.getMerchant();
            if (merchant == null) {
                throw new DataNotFoundException("Merchant profile not found");
            }
            if (!Boolean.TRUE.equals(merchant.getIsActive())) {
                throw new UnauthorizedException("Merchant account is inactive");
            }
            String token = jwtUtil.generateToken(account.getEmail(), account.getRole().name(), merchant.getId());
            return accountMapper.toLoginResponse(merchant, token);
        }

        if (account.getRole() == RoleEnum.SUPER_ADMIN) {
            String token = jwtUtil.generateToken(account.getEmail(), account.getRole().name(), account.getId());
            ResLoginDto response = new ResLoginDto();
            response.setToken(token);
            return response;
        }

        throw new UnauthorizedException("Unsupported account role");
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
        
        walletService.createWallet(savedUser.getId(), OwnerTypeEnum.USER);
        
        return userMapper.toRegisterResponse(savedUser);
    }

    @Override
    @Transactional
    public ResRegisterMerchantDto registerMerchant(ReqRegisterMerchantDto request) {
        ResMerchantDto merchant = merchantService.createMerchant(request);
        return merchantMapper.toRegisterResponse(merchant);
    }
}
