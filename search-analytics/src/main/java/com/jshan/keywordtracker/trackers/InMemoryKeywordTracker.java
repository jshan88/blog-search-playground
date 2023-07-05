package com.jshan.keywordtracker.trackers;

import com.jshan.keywordtracker.KeywordTracker;
import com.jshan.persistence.KeywordCount;
import com.jshan.persistence.database.entity.TopKeyword;
import com.jshan.persistence.database.repository.TopKeywordRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * In-memory 사용하여 구현한 KeywordTracker
 */
@Component
@RequiredArgsConstructor
public class InMemoryKeywordTracker implements KeywordTracker {

    private final Map<String, Integer> keywordCounts;
    private final Queue<KeywordCount> topKeywords;
    private final TopKeywordRepository topKeywordRepository;

    @Override
    public void onSearch(String keyword) {
        keywordCounts.merge(keyword, 1, Integer::sum);
        updateTopKeywords(keyword, keywordCounts.get(keyword));
    }

    private void updateTopKeywords(String keyword, int count) {
        topKeywords.removeIf(kc -> kc.getKeyword().equals(keyword));
        KeywordCount keywordCount = new KeywordCount(keyword, count);
        topKeywords.add(keywordCount);
    }

    /**
     * 인기 있는 키워드 목록을 검색. 키워드는 검색 횟수를 기준으로 내림차순 정렬 <br>
     * 시스템 재부팅 등의 사유로 topKeywords 가 비어있을 시, DB 로부터 다시 가져옴.
     *
     * @return Top 10 KeywordCount 객체 리스트
     */
    @Override
    public List<KeywordCount> getPopularKeywords() {
        if(topKeywords.isEmpty()) {
            List<TopKeyword> databaseTopKeywords = topKeywordRepository.findAll();
            List<KeywordCount> keywordCountList = databaseTopKeywords.stream()
                .map(topKeyword -> KeywordCount.builder()
                    .keyword(topKeyword.getKeyword())
                    .count(topKeyword.getCount())
                    .build()).toList();
            topKeywords.addAll(keywordCountList);
        }
        List<KeywordCount> popularKeywords = new ArrayList<>(topKeywords);
        popularKeywords.sort(Comparator.reverseOrder());

        return popularKeywords;
    }
}
