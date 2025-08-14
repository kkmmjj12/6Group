package com.example.review.Service;

import com.example.review.Entity.ReviewEntity;
import com.example.review.Entity.ReviewImageEntity;
import com.example.review.Repository.ReviewImageRepository;
import com.example.review.Repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Service
@RequiredArgsConstructor

public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OpenAIService openAIService;
    private final ReviewImageRepository reviewImageRepository;


    // 리뷰 저장
    public ReviewEntity saveReview(String author, String content) {
        ReviewEntity review = ReviewEntity.builder()
                .author(author)
                .content(content)
                .build();
        return  reviewRepository.save(review);
    }

    //리뷰 + 이미지 저장
    public ReviewEntity saveReviewWithImages(String author, String content, MultipartFile[] images) {

        ReviewEntity review = ReviewEntity.builder()
                .author(author)
                .content(content)
                .build();
        review = reviewRepository.save(review);

        // 2. 이미지 처리
        if (images != null && images.length > 0) {
            int count = Math.min(images.length, 3); // 최대 3장
            List<ReviewImageEntity> imageEntities = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                MultipartFile file = images[i];
                try {
                    String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    Path uploadPath = Paths.get("uploads/" + fileName);

                    // 디렉토리 없으면 생성
                    Files.createDirectories(uploadPath.getParent());

                    Files.copy(file.getInputStream(), uploadPath, StandardCopyOption.REPLACE_EXISTING);


                    ReviewImageEntity imageEntity = ReviewImageEntity.builder()
                            .imagePath(uploadPath.toString())
                            .review(review)
                            .build();
                    imageEntities.add(imageEntity);
                } catch (IOException e) {
                    throw new RuntimeException("이미지 저장 실패", e);
                }
            }
            reviewImageRepository.saveAll(imageEntities);
            review.setImages(imageEntities);
        }

        return review;
    }


    //모든 리뷰 조회
    public List<ReviewEntity> getAllReviews() {
        return reviewRepository.findAll();
    }

    //리뷰 조회
    public ReviewEntity getReview(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("리뷰가 존재하지 않습니다"));
    }

    //리뷰 수정
    public ReviewEntity updateReview(Long id, String content) {
        ReviewEntity review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("리뷰가 존재하지 않습니다"));
        review.setContent(content);
        return reviewRepository.save(review);
    }

    // 리뷰 요약
    public String summarizeReview(String content) {
        String prompt = "다음 리뷰를 요약해줘: " + content;
        return openAIService.getChatResponse(prompt); //OpenAI에게 prompt 전달하면 리뷰 요약 결과 반환
    }

    // AI 챗봇
    public String chatWithAI(String question) {
        return openAIService.getChatResponse(question);
    }

    //리뷰 삭제
    public void deleteReview(Long id) {
        ReviewEntity review = reviewRepository.findById(id).orElseThrow(()
                -> new RuntimeException("리뷰가 존재하지 않습니다"));
        reviewRepository.delete(review);    }
}


