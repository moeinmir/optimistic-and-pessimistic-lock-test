package com.meb.account_management.repository;

import com.meb.account_management.model.CustomUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomUserRepository extends JpaRepository<CustomUser,Long> {

    Optional<CustomUser> findByUsername(String userName);
}
