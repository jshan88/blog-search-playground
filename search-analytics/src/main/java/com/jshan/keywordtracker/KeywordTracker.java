package com.jshan.keywordtracker;

import com.jshan.persistence.KeywordCount;
import java.util.List;

/**
 * 검색 이벤트에 기반하여 키워드의 해당 키워드의 Popularity 를 추적하는 클래스 <br>
 * 키워드 검색 횟수를 업데이트하고 인기 있는 키워드 목록을 검색하는 메서드를 제공 <br>
 */
public interface KeywordTracker {

    /**
     * 검색 이벤트에 기반하여 키워드 검색 횟수와 인기 있는 키워드 목록을 업데이트
     *
     * @param keyword 검색 키워드
     */
    void onSearch(String keyword);

    /**
     * 인기 있는 키워드 목록을 검색 <br>
     *
     * @return 가장 많이 조회된 키워드 리스트
     */
    List<KeywordCount> getPopularKeywords();

}
