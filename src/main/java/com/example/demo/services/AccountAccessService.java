package com.example.demo.services;

import com.example.demo.entities.AccountEntity;

public interface AccountAccessService {
    void assertCanLogin(AccountEntity account);

    void assertCanAccessPayments(AccountEntity account);
}
