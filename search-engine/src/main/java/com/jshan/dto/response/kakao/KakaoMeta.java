package com.jshan.dto.response.kakao;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoMeta {
    private int total_count;
    private int pageable_count;
    private boolean is_end;

    @Builder
    public KakaoMeta(int total_count, int pageable_count, boolean is_end) {
        this.total_count = total_count;
        this.pageable_count = pageable_count;
        this.is_end = is_end;
    }
}
