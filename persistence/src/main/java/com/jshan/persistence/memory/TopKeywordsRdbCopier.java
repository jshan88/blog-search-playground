package com.jshan.persistence.memory;

import com.jshan.persistence.KeywordCount;
import com.jshan.persistence.database.entity.TopKeyword;
import com.jshan.persistence.database.repository.TopKeywordRepository;
import java.util.List;
import java.util.Queue;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 시스템 재부팅 등으로 인한 In-memory 데이터 손실을 대비해 Top Keywords 를 RDB 로 카피해둠
 */
@Component
@RequiredArgsConstructor
public class TopKeywordsRdbCopier {

    private final Queue<KeywordCount> topKeywords;
    private final TopKeywordRepository topKeywordRepository;
    private boolean isCopyingToRdb = false;

    /**
     * 일정 간격으로 인메모리 데이터를 데이터베이스로 복사 수행 (60초마다 수행)
     */
    @Scheduled(fixedDelay = 60000)
    public void copyToRdb() {
        if (isCopyingToRdb) {
            return;
        }

        synchronized (topKeywords) {
            if(!topKeywords.isEmpty()) {
                isCopyingToRdb = true;

                List<TopKeyword> keywords = topKeywords.stream()
                    .map(topKeyword -> TopKeyword.builder()
                        .keyword(topKeyword.getKeyword())
                        .count(topKeyword.getCount())
                        .build()).toList();

                topKeywordRepository.deleteAllInBatch();
                topKeywordRepository.saveAll(keywords);
                isCopyingToRdb = false;
            }
        }
    }
}
