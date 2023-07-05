package com.jshan.dto.response;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 검색 결과를 나타내는 클래스
 */
@Getter
@NoArgsConstructor
public class SearchResult {

    /**
     * 총 항목 개수
     */
    private int totalCount;

    /**
     * 총 페이지 수
     */
    private int totalPage;

    /**
     * 현재 페이지 번호
     */
    private int currentPage;

    /**
     * 검색 결과 항목 리스트
     */
    private List<Document> documents = new ArrayList<>();

    /**
     *
     * @param totalCount  총 항목 개수
     * @param totalPage   총 페이지 수
     * @param currentPage 현재 페이지 번호
     * @param documents   검색 결과 항목 리스트
     */
    @Builder
    public SearchResult(int totalCount, int totalPage, int currentPage, List<Document> documents) {
        this.totalCount = totalCount;
        this.totalPage = totalPage;
        this.currentPage = currentPage;
        this.documents = documents;
    }
}
