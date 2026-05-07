package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.RedisToken;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RedisTokenRepository extends CrudRepository<RedisToken, String> {
    void deleteAllEmail(String email);
    List<RedisToken> findByEmail(String email);
}
