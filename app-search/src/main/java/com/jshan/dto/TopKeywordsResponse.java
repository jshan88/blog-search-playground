package com.jshan.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TopKeywordsResponse {
    private String keyword;
    private int count;

    /**
     * 지정된 키워드와 검색 카운트 값으로 KeywordCount 인스턴스 생성
     *
     * @param keyword Tracking Keyword
     * @param count   Keyword 검색 카운트
     */
    @Builder
    public TopKeywordsResponse(String keyword, int count) {
        this.keyword = keyword;
        this.count = count;
    }
}
