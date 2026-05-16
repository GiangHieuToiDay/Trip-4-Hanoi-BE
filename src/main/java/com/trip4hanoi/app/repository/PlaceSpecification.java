package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.dto.req.PlaceFilterRequest;
import com.trip4hanoi.app.entity.Place;
import com.trip4hanoi.app.util.StringUtil;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PlaceSpecification {
    public static Specification<Place> filterPlaces(PlaceFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // lọc bỏ địa điểm đã xóa
            predicates.add(cb.equal(root.get("deleted"), false));

            // 1. Lọc theo từ khóa (Thông minh: Không dấu + Không phân biệt hoa thường)
            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                String keyword = StringUtil.removeAccents(request.getKeyword().trim());
                String pattern = "%" + keyword + "%";

                predicates.add(cb.like(cb.lower(root.get("searchVector")), pattern));
            }

            // 2. Lọc theo Category ID
            if (request.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), request.getCategoryId()));
            }

            // 3. Lọc theo Quận/Huyện
            if (request.getDistrict() != null && !request.getDistrict().trim().isEmpty()) {
                predicates.add(cb.equal(root.get("district"), request.getDistrict().trim()));
            }

            // 4. Lọc theo khoảng giá
            if (request.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("priceAvg"), request.getMinPrice()));
            }
            if (request.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("priceAvg"), request.getMaxPrice()));
            }

            // 5. Lọc theo Rating (Lấy từ mức điểm này trở lên)
            if (request.getMinRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("ratingAvg"), request.getMinRating()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
