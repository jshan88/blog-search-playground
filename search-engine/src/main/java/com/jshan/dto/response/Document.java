package com.jshan.dto.response;

import com.jshan.dto.response.kakao.KakaoDocument;
import com.jshan.dto.response.naver.NaverDocument;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Document {
    private String title;
    private String contents;
    private String url;
    private String blogName;
    private String thumbnail;
    private String dateTime;

    @Builder
    public Document(String title, String contents, String url, String blogName, String thumbnail, String dateTime) {
        this.title = title;
        this.contents = contents;
        this.url = url;
        this.blogName = blogName;
        this.thumbnail = thumbnail;
        this.dateTime = dateTime;
    }

    public static Document fromKakao(KakaoDocument document) {
        return Document.builder()
            .title(document.getTitle())
            .contents(document.getContents())
            .url(document.getUrl())
            .blogName(document.getBlogname())
            .thumbnail(document.getThumbnail())
            .dateTime(document.getDatetime())
            .build();
    }

    public static Document fromNaver(NaverDocument document) {
        return Document.builder()
            .title(document.getTitle())
            .contents(document.getDescription())
            .url(document.getLink())
            .blogName(document.getBloggername())
            .dateTime(document.getPostdate())
            .build();
    }
}

