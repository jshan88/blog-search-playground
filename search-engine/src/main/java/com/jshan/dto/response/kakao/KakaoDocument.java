package com.jshan.dto.response.kakao;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoDocument {
    private String title;
    private String contents;
    private String url;
    private String blogname;
    private String thumbnail;
    private String datetime;

    @Builder
    public KakaoDocument(String title, String contents, String url, String blogname, String thumbnail, String datetime) {
        this.title = title;
        this.contents = contents;
        this.url = url;
        this.blogname = blogname;
        this.thumbnail = thumbnail;
        this.datetime = datetime;
    }
}
