package com.example.review.Repository;

import com.example.review.Entity.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<StoreEntity, Long> {
    List<StoreEntity> findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(String name, String category);
}
