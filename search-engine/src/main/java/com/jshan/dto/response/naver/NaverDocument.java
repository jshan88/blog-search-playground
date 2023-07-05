package com.jshan.dto.response.naver;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverDocument {
    private String title;
    private String link;
    private String description;
    private String bloggername;
    private String bloggerlink;
    private String postdate;

    @Builder
    public NaverDocument(String title, String link, String description, String bloggername, String bloggerlink, String postdate) {
        this.title = title;
        this.link = link;
        this.description = description;
        this.bloggername = bloggername;
        this.bloggerlink = bloggerlink;
        this.postdate = postdate;
    }
}