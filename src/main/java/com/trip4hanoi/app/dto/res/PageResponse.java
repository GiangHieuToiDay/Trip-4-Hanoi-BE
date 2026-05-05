package com.trip4hanoi.app.dto.res;

import lombok.*;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> implements Serializable {
    private int pageNumber;
    private int pageSize;
    private long totalPages;
    private long totalElements;
    private List<T> data; //Dữ liệu (User, Room, Permission...)

    /**
     *  Hàm tiện ích (Static Factory Method) giúp convert nhanh từ Page của Spring
     * @param pageInfo: Đối tượng Page lấy từ Repository
     * @param data : List DTO đã được convert qua MapStruct
     * @return
     * @param <T>
     */
    public static <T> PageResponse<T> from(Page<?> pageInfo , List<T> data){
        return PageResponse.<T>builder()
                .pageNumber(pageInfo.getNumber() + 1)
                .pageSize(pageInfo.getSize())
                .totalPages(pageInfo.getTotalPages())
                .totalElements(pageInfo.getTotalElements())
                .data(data)
                .build();
    }
}
