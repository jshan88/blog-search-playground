package com.jshan.dto.response.kakao;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoResponse {
    private KakaoMeta meta;
    private List<KakaoDocument> documents;

    @Builder
    public KakaoResponse(KakaoMeta meta, List<KakaoDocument> documents) {
        this.meta = meta;
        this.documents = documents;
    }
}
