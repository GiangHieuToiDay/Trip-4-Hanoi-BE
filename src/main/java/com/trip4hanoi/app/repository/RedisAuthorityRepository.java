package com.trip4hanoi.app.repository;


import com.trip4hanoi.app.entity.RedisAuthority;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedisAuthorityRepository extends CrudRepository<RedisAuthority, String> {
}
