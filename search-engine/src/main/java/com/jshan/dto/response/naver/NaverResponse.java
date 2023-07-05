package com.jshan.dto.response.naver;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverResponse {

    private String lastBuildDate;
    private int total;
    private int start;
    private int display;
    private List<NaverDocument> items;

    @Builder
    public NaverResponse(String lastBuildDate, int total, int start, int display, List<NaverDocument> items) {
        this.lastBuildDate = lastBuildDate;
        this.total = total;
        this.start = start;
        this.display = display;
        this.items = items;
    }
}
