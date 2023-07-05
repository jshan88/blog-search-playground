package com.jshan.persistence;

import java.util.Objects;
import lombok.Builder;
import lombok.Getter;

/**
 * Popularity Tracking 을 위한 키워드와 검색 카운트 나타내는 클래스
 */
@Getter
public class KeywordCount implements Comparable<KeywordCount> {

    private String keyword;
    private int count;

    /**
     * 지정된 키워드와 검색 카운트 값으로 KeywordCount 인스턴스 생성
     *
     * @param keyword Tracking Keyword
     * @param count   Keyword 검색 카운트
     */
    @Builder
    public KeywordCount(String keyword, int count) {
        this.keyword = keyword;
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KeywordCount that)) {
            return false;
        }
        return count == that.count && keyword.equals(that.keyword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyword, count);
    }

    @Override
    public int compareTo(KeywordCount o) {
        return Integer.compare(this.count, o.count);
    }
}
