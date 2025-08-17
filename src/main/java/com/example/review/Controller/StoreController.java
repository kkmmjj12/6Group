package com.example.review.Controller;

import com.example.review.Entity.StoreEntity;
import com.example.review.Service.StoreService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    // GET /stores/search?keyword=치킨
    @GetMapping("/search")
    public List<StoreEntity> searchStores(@RequestParam String keyword) {
        return storeService.searchStores(keyword);
    }
}
