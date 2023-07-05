package com.jshan.dto.request;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SortType {


    /**
     * 정확도순 정렬
     */
    ACCURACY("accuracy"),

    /**
     * 최신순 정렬
     */
    RECENCY("recency");

    private final String value;

    public String value() {
        return value;
    }
}
