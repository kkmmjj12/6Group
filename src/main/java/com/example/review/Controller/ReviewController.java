package com.example.review.Controller;

import com.example.review.Entity.ReviewEntity;
import com.example.review.Service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;


    @PostMapping
    public ResponseEntity<ReviewEntity> addReview(@RequestParam String author,
                                                  @RequestParam String content) {
        return ResponseEntity.ok(reviewService.saveReview(author, content));
    }


    @PostMapping("/with-image")
    public ResponseEntity<ReviewEntity> addReviewWithImage(@RequestParam String author,
                                                           @RequestParam String content,
                                                           @RequestParam(required = false) MultipartFile[] images)
    {
        return ResponseEntity.ok(reviewService.saveReviewWithImages(author, content, images));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ReviewEntity>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewEntity> getReview(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReview(id));
    }

        @PutMapping("/{id}")
    public ResponseEntity<ReviewEntity> updateReview(@PathVariable Long id,
                                                     @RequestParam String content) {
        return ResponseEntity.ok(reviewService.updateReview(id, content));
    }

    @PostMapping("/summarize")
    public Map<String,String> summarize(@RequestBody Map<String,String> request) {
        String summary = reviewService.summarizeReview(request.get("content"));
        return Map.of("summary", summary);
    }


    @PostMapping("/chat")
    public Map<String,String> chat(@RequestBody Map<String,String> request) {
        String answer = reviewService.chatWithAI(request.get("question"));
        return Map.of("answer", answer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok("삭제 성공");
    }
}
