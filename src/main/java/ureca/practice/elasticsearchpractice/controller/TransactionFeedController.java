package ureca.practice.elasticsearchpractice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ureca.practice.elasticsearchpractice.dto.FeedDto;
import ureca.practice.elasticsearchpractice.service.TransactionFeedService;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class TransactionFeedController {

    private final TransactionFeedService feedService;

    // 게시글 생성
    @PostMapping
    public ResponseEntity<FeedDto.Response> createFeed(@RequestBody FeedDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(feedService.createFeed(request));
    }

    // 게시글 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<FeedDto.Response> getFeed(@PathVariable("id") String id) {
        return ResponseEntity.ok(feedService.getFeed(id));
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<FeedDto.Response> updateFeed(@PathVariable("id") String id, @RequestBody FeedDto.Request request) {
        return ResponseEntity.ok(feedService.updateFeed(id, request));
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeed(@PathVariable("id") String id) {
        feedService.deleteFeed(id);
        return ResponseEntity.noContent().build();
    }


    // --- 통합 검색 엔드포인트 ---
    /**
     * 하나의 검색어로 제목, 내용, 닉네임, 통신사, 데이터양을 모두 검색합니다.
     * 예시: /api/feeds/search?query=SKT 데이터 10기가
     * @param query 검색어
     * @param pageable 페이징 정보
     * @return 검색 결과 페이지
     */
    @GetMapping("/search")
    public ResponseEntity<Page<FeedDto.Response>> search(
            @RequestParam(name = "query") String query,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // 서비스의 새로운 통합 검색 메서드를 호출합니다.
        return ResponseEntity.ok(feedService.unifiedSearch(query, pageable));
    }
}
