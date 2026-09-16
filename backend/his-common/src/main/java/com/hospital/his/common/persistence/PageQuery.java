package com.hospital.his.common.persistence;

public record PageQuery(int page, int size) {
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public PageQuery {
        if (page < 1) {
            throw new IllegalArgumentException("page must be greater than or equal to 1");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and " + MAX_SIZE);
        }
    }

    public static PageQuery of(Integer page, Integer size) {
        return new PageQuery(page == null ? 1 : page, size == null ? DEFAULT_SIZE : size);
    }

    public long offset() {
        return (long) (page - 1) * size;
    }
}
