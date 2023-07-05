package com.jshan.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class SearchParam {

    @NotNull
    private String query;

    private SortType sort = SortType.ACCURACY; // default 정확도 순

    @Min(value = 1, message = "페이지 번호는 최소값 1 입니다.")
    @Max(value = 50, message = "페이지 번호는 최대값 50 입니다.")
    private int page = 1;

    @Min(value = 1, message = "페이지 당 표출 문서 수는 최소 1개 이상입니다.")
    @Max(value = 50, message = "페이지 당 표츌 문서 수는 최대 50개입니다.")
    private int size = 10;

    @Builder
    public SearchParam(String query, SortType sort, int page, int size) {
        this.query = query;
        this.sort = sort;
        this.page = page;
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SearchParam that)) {
            return false;
        }
        return page == that.page && size == that.size && query.equals(that.query) && sort == that.sort;
    }

    @Override
    public int hashCode() {
        return Objects.hash(query, sort, page, size);
    }
}
