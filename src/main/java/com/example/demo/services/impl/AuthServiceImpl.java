package com.example.demo.services.impl;

import com.example.demo.dtos.requests.ReqLoginDto;
import com.example.demo.dtos.requests.ReqRegisterDto;
import com.example.demo.dtos.responses.ResLoginDto;
import com.example.demo.dtos.responses.ResRegisterDto;
import com.example.demo.entities.AccountEntity;
import com.example.demo.entities.UserEntity;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.exceptions.DuplicateResourceException;
import com.example.demo.mappers.AccountMapper;
import com.example.demo.mappers.UserMapper;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.AuthService;
import com.example.demo.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final UserMapper userMapper;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public ResLoginDto login(ReqLoginDto request) {
        Optional<AccountEntity> accountOpt = accountRepository.findByEmail(request.getEmail());
        if (accountOpt.isEmpty()) {
            throw new DataNotFoundException("Account not found");
        }
        AccountEntity account = accountOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new BadRequestException("Email and password doesn't match");
        }
        UserEntity user = userRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new DataNotFoundException("User profile not found for this account"));

        return accountMapper.toLoginResponse(account, user, jwtUtil.generateToken(account.getEmail(), account.getId()));
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
}
