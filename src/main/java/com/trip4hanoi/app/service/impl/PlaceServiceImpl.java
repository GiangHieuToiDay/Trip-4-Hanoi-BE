package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.dto.req.PlaceFilterRequest;
import com.trip4hanoi.app.dto.req.PlaceRequest;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.dto.res.PlaceDetailResponse;
import com.trip4hanoi.app.dto.res.PlaceResponse;
import com.trip4hanoi.app.entity.Category;
import com.trip4hanoi.app.entity.Place;
import com.trip4hanoi.app.exception.AppException;
import com.trip4hanoi.app.exception.ErrorCode;
import com.trip4hanoi.app.mapper.PlaceMapper;
import com.trip4hanoi.app.repository.CategoryRepository;
import com.trip4hanoi.app.repository.PlaceRepository;
import com.trip4hanoi.app.repository.PlaceSpecification;
import com.trip4hanoi.app.service.PlaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "PLACE-SERVICE")
public class PlaceServiceImpl implements PlaceService {
    private final PlaceRepository placeRepository;
    private final CategoryRepository categoryRepository;
    private final PlaceMapper placeMapper;

    @Override
    public List<PlaceResponse> getAllPlaces(Long categoryId) {
        List<Place> places;
        if (categoryId != null) {
            places = placeRepository.findByCategoryId(categoryId);
        } else {
            places = placeRepository.findAll();
        }
        return places.stream()
                .map(placeMapper::toPlaceResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PlaceDetailResponse getPlaceDetail(Long id) {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PLACE_NOT_FOUND));
        return placeMapper.toPlaceDetailResponse(place);
    }

    @Override
    @Transactional
    public PlaceResponse createPlace(PlaceRequest request) {
        Place place = placeMapper.toPlace(request);
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            place.setCategory(category);
        }
        return placeMapper.toPlaceResponse(placeRepository.save(place));
    }

    @Override
    @Transactional
    public PlaceResponse updatePlace(Long id, PlaceRequest request) {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PLACE_NOT_FOUND));
        
        placeMapper.updatePlace(place, request);
        
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            place.setCategory(category);
        }
        
        return placeMapper.toPlaceResponse(placeRepository.save(place));
    }

    @Override
    @Transactional
    public void deletePlace(Long id) {
        if (!placeRepository.existsById(id)) {
            throw new AppException(ErrorCode.PLACE_NOT_FOUND);
        }
        placeRepository.deleteById(id);
    }

    @Override
    public PageResponse<PlaceResponse> searchPlaces(PlaceFilterRequest request){

        //Tạo Pageable
        Pageable pageable = PageRequest.of(request.getPage()-1 ,request.getSize());

        //Lấy tất cả các bản ghi thỏa mãn bộ lọc (chưa tính khoảng cách)
        Specification<Place> spec = PlaceSpecification.filterPlaces(request);


        //Trường hợp 1: Không có tọa độ (Lọc cơ bản)
        if(request.getUserLat() == null || request.getUserLng() == null){
            Page<Place> placePage = placeRepository.findAll(spec,pageable);
            List<PlaceResponse> data = placePage.getContent().stream()
                    .map(placeMapper ::toPlaceResponse)
                    .collect(Collectors.toList());
            return  PageResponse.from(placePage,data);
        }


        //Trường hợp 2: Có tọa độ (Tìm quanh đây -Phức tạp hơn)
        //vì tính khoảng cách cần tất cả kết quả để lọc radius và sắp xếp,
        // ta lấy hết List phù hợp spec về Java xử lý
        List<Place> allMatches = placeRepository.findAll(spec);

        List<PlaceResponse> allResponses =  allMatches.stream()
                .map(place -> {
                    PlaceResponse res = placeMapper.toPlaceResponse(place);
                    Double dist = calculateHaversine(request.getUserLat(), request.getUserLng(), place.getLatitude(), place.getLongitude());

                    res.setDistance(dist);// dist có thể là null nếu dữ liệu DB thiếu

                    //[TODO: USER_AUTH] sau check UserPreference ở  đây để set isRecommended
                    return res;
                })

                // Lọc theo bán kính người dùng chọn
                .filter(res ->  {
                    if (res.getDistance() == null) return false; // Không có tọa độ thì loại khỏi kết quả "Tìm quanh đây"
                    return request.getRadius() == null || res.getDistance() <= request.getRadius();
                })

                // Sắp xếp theo khoảng cách gần nhất
                .sorted(Comparator.comparing(PlaceResponse::getDistance))
                .collect(Collectors.toList());

        // Thực hiện phân trang thủ công cho List (Manual Pagination)
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allResponses.size());

        List<PlaceResponse> pagedData = (start <= end) ? allResponses.subList(start, end) : new ArrayList<>();

        //Tạo đối tượng Page thủ công để dùng được hàm PageResponse.from
        Page<PlaceResponse> manualPage = new PageImpl<>(pagedData, pageable, allResponses.size());

        return  PageResponse.from(manualPage,pagedData);



    }

    @Override
    public PageResponse<PlaceResponse> getAllPlacesForAdmin(String keyword, Long categoryId, String district, String sort, int page, int size) {
        log.info("Admin fetching places - keyword: {}, category: {}, sort: {}, page: {}", keyword, categoryId, sort, page);

        // Logic xử lý sort
        // mặc định sắp xếp theo id giảm dần (mới nhất lên đầu)
        Sort.Order order = new Sort.Order(Sort.Direction.DESC, "id");

        if(StringUtils.hasLength(sort) && sort.contains(":")){
             String[] parts = sort.split(":");
             String filed = parts[0];
             String direction = parts[1];
             order = new Sort.Order(direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, filed);

        }

        // Pageable
        int pageNo = page > 0 ?  page - 1 : 0;
        Pageable pageable = PageRequest.of(pageNo, size, Sort.by(order));

        //Sử dụng Specification
        //Tạo 1 request giả lập để dùng lại spec
        PlaceFilterRequest adminFilter = new PlaceFilterRequest();
        adminFilter.setKeyword(keyword);
        adminFilter.setCategoryId(categoryId);
        adminFilter.setDistrict(district);

        Specification<Place> spec = PlaceSpecification.filterPlaces(adminFilter);


        // Truy vấn Database
        Page<Place> pageResult = placeRepository.findAll(spec, pageable);

        //convert
        List<PlaceResponse> responses = pageResult.getContent().stream()
                .map(placeMapper::toPlaceResponse)
                .toList();

        return PageResponse.from(pageResult,responses);
    }


    /**
     * Công thức Haversince
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
    private  Double calculateHaversine(Double lat1, Double lon1  , Double lat2 , Double lon2){

        // Nếu bất kỳ tọa độ nào bị null, không thể tính toán, trả về null hoặc một giá trị mặc định
        if (lat1 == null || lat2 == null || lon1 == null || lon2 == null) {
            return null;
        }

        double R = 6371; //km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                           Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return Math.round(R * c * 100.0) / 100.0;
    }


}
