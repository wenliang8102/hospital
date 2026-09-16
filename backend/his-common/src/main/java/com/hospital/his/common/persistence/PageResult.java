package com.hospital.his.common.persistence;

import java.util.List;

public record PageResult<T>(List<T> items, int page, int size, long total) {
    public PageResult {
        items = List.copyOf(items);
    }

    public static <T> PageResult<T> of(List<T> items, PageQuery query, long total) {
        return new PageResult<>(items, query.page(), query.size(), total);
    }
}
