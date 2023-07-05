package com.jshan.persistence.database.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Keyword Tracker 로 In-memory 사용 시, 주기적으로 Top Keyword 정보를 DB 에 Copy 함. <br>
 * 이에 해당하는 Entity Class.
 */
@Getter
@NoArgsConstructor
@Entity
public class TopKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String keyword;
    private int count;

    @Builder
    public TopKeyword(String keyword, int count) {
        this.keyword = keyword;
        this.count = count;
    }
}
