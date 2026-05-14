package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.service.GeocodingService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GeocodingServiceImpl implements GeocodingService {

    private static class Point {
        double lat, lng;
        Point(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }

    private static class DistrictPolygon {
        String name;
        List<Point> vertices;

        DistrictPolygon(String name, double[][] coords) {
            this.name = name;
            this.vertices = new ArrayList<>();
            for (double[] coord : coords) {
                vertices.add(new Point(coord[0], coord[1]));
            }
        }
    }

    private final List<DistrictPolygon> hanoiDistricts = new ArrayList<>();

    public GeocodingServiceImpl() {
        // Khởi tạo ranh giới đơn giản hóa (Bounding Box mở rộng thành Polygon) cho các quận huyện Hà Nội
        // Lưu ý: Đây là tọa độ xấp xỉ bao quát các vùng hành chính chính
        
        // 1. Quận Ba Đình
        hanoiDistricts.add(new DistrictPolygon("Ba Đình", new double[][]{
            {21.028, 105.810}, {21.050, 105.810}, {21.050, 105.845}, {21.028, 105.845}
        }));

        // 2. Quận Hoàn Kiếm
        hanoiDistricts.add(new DistrictPolygon("Hoàn Kiếm", new double[][]{
            {21.020, 105.840}, {21.040, 105.840}, {21.040, 105.865}, {21.020, 105.865}
        }));

        // 3. Quận Tây Hồ
        hanoiDistricts.add(new DistrictPolygon("Tây Hồ", new double[][]{
            {21.050, 105.790}, {21.095, 105.790}, {21.095, 105.850}, {21.050, 105.850}
        }));

        // 4. Quận Cầu Giấy
        hanoiDistricts.add(new DistrictPolygon("Cầu Giấy", new double[][]{
            {21.015, 105.775}, {21.050, 105.775}, {21.050, 105.810}, {21.015, 105.810}
        }));

        // 5. Quận Đống Đa
        hanoiDistricts.add(new DistrictPolygon("Đống Đa", new double[][]{
            {21.005, 105.815}, {21.030, 105.815}, {21.030, 105.840}, {21.005, 105.840}
        }));

        // 6. Quận Hai Bà Trưng
        hanoiDistricts.add(new DistrictPolygon("Hai Bà Trưng", new double[][]{
            {20.990, 105.840}, {21.020, 105.840}, {21.020, 105.875}, {20.990, 105.875}
        }));

        // 7. Quận Thanh Xuân
        hanoiDistricts.add(new DistrictPolygon("Thanh Xuân", new double[][]{
            {20.985, 105.795}, {21.010, 105.795}, {21.010, 105.825}, {20.985, 105.825}
        }));

        // 8. Quận Hoàng Mai
        hanoiDistricts.add(new DistrictPolygon("Hoàng Mai", new double[][]{
            {20.950, 105.820}, {20.995, 105.820}, {20.995, 105.910}, {20.950, 105.910}
        }));

        // 9. Quận Long Biên
        hanoiDistricts.add(new DistrictPolygon("Long Biên", new double[][]{
            {21.010, 105.860}, {21.090, 105.860}, {21.090, 105.950}, {21.010, 105.950}
        }));

        // 10. Quận Hà Đông
        hanoiDistricts.add(new DistrictPolygon("Hà Đông", new double[][]{
            {20.920, 105.720}, {20.990, 105.720}, {20.990, 105.800}, {20.920, 105.800}
        }));

        // 11. Quận Nam Từ Liêm
        hanoiDistricts.add(new DistrictPolygon("Nam Từ Liêm", new double[][]{
            {20.980, 105.730}, {21.030, 105.730}, {21.030, 105.785}, {20.980, 105.785}
        }));

        // 12. Quận Bắc Từ Liêm
        hanoiDistricts.add(new DistrictPolygon("Bắc Từ Liêm", new double[][]{
            {21.030, 105.710}, {21.110, 105.710}, {21.110, 105.790}, {21.030, 105.790}
        }));

        // 13. Huyện Gia Lâm
        hanoiDistricts.add(new DistrictPolygon("Gia Lâm", new double[][]{
            {20.950, 105.910}, {21.080, 105.910}, {21.080, 106.020}, {20.950, 106.020}
        }));

        // 14. Huyện Đông Anh
        hanoiDistricts.add(new DistrictPolygon("Đông Anh", new double[][]{
            {21.080, 105.750}, {21.210, 105.750}, {21.210, 105.930}, {21.080, 105.930}
        }));

        // 15. Huyện Sóc Sơn
        hanoiDistricts.add(new DistrictPolygon("Sóc Sơn", new double[][]{
            {21.210, 105.700}, {21.360, 105.700}, {21.360, 105.950}, {21.210, 105.950}
        }));

        // 16. Huyện Quốc Oai (Nơi UserId 7 đang ở)
        hanoiDistricts.add(new DistrictPolygon("Quốc Oai", new double[][]{
            {20.880, 105.500}, {21.020, 105.500}, {21.020, 105.680}, {20.880, 105.680}
        }));

        // 17. Huyện Thạch Thất
        hanoiDistricts.add(new DistrictPolygon("Thạch Thất", new double[][]{
            {20.950, 105.450}, {21.120, 105.450}, {21.120, 105.620}, {20.950, 105.620}
        }));

        // 18. Huyện Chương Mỹ
        hanoiDistricts.add(new DistrictPolygon("Chương Mỹ", new double[][]{
            {20.750, 105.550}, {20.950, 105.550}, {20.950, 105.750}, {20.750, 105.750}
        }));

        // 19. Huyện Thanh Trì
        hanoiDistricts.add(new DistrictPolygon("Thanh Trì", new double[][]{
            {20.910, 105.800}, {20.970, 105.800}, {20.970, 105.870}, {20.910, 105.870}
        }));

        // 20. Huyện Mê Linh
        hanoiDistricts.add(new DistrictPolygon("Mê Linh", new double[][]{
            {21.120, 105.630}, {21.240, 105.630}, {21.240, 105.780}, {21.120, 105.780}
        }));

        // 21. Huyện Hoài Đức
        hanoiDistricts.add(new DistrictPolygon("Hoài Đức", new double[][]{
            {20.980, 105.650}, {21.090, 105.650}, {21.090, 105.750}, {20.980, 105.750}
        }));

        // 22. Huyện Đan Phượng
        hanoiDistricts.add(new DistrictPolygon("Đan Phượng", new double[][]{
            {21.080, 105.620}, {21.160, 105.620}, {21.160, 105.730}, {21.080, 105.730}
        }));

        // 23. Thị xã Sơn Tây
        hanoiDistricts.add(new DistrictPolygon("Sơn Tây", new double[][]{
            {21.050, 105.350}, {21.180, 105.350}, {21.180, 105.550}, {21.050, 105.550}
        }));

        // 24. Huyện Ba Vì
        hanoiDistricts.add(new DistrictPolygon("Ba Vì", new double[][]{
            {21.020, 105.280}, {21.250, 105.280}, {21.250, 105.530}, {21.020, 105.530}
        }));

        // 25. Huyện Mỹ Đức
        hanoiDistricts.add(new DistrictPolygon("Mỹ Đức", new double[][]{
            {20.580, 105.680}, {20.850, 105.680}, {20.850, 105.830}, {20.580, 105.830}
        }));

        // 26. Huyện Ứng Hòa
        hanoiDistricts.add(new DistrictPolygon("Ứng Hòa", new double[][]{
            {20.620, 105.750}, {20.880, 105.750}, {20.880, 105.880}, {20.620, 105.880}
        }));

        // 27. Huyện Phú Xuyên
        hanoiDistricts.add(new DistrictPolygon("Phú Xuyên", new double[][]{
            {20.620, 105.850}, {20.820, 105.850}, {20.820, 106.010}, {20.620, 106.010}
        }));

        // 28. Huyện Thường Tín
        hanoiDistricts.add(new DistrictPolygon("Thường Tín", new double[][]{
            {20.780, 105.830}, {20.920, 105.830}, {20.920, 105.950}, {20.780, 105.950}
        }));

        // 29. Huyện Thanh Oai
        hanoiDistricts.add(new DistrictPolygon("Thanh Oai", new double[][]{
            {20.780, 105.720}, {20.950, 105.720}, {20.950, 105.850}, {20.780, 105.850}
        }));

        // 30. Huyện Phúc Thọ
        hanoiDistricts.add(new DistrictPolygon("Phúc Thọ", new double[][]{
            {21.050, 105.510}, {21.180, 105.510}, {21.180, 105.650}, {21.050, 105.650}
        }));
    }

    @Override
    public String getDistrictFromCoords(Double lat, Double lng) {
        if (lat == null || lng == null) return null;

        for (DistrictPolygon district : hanoiDistricts) {
            if (isPointInPolygon(lat, lng, district.vertices)) {
                return district.name;
            }
        }

        // Kiểm tra phạm vi tổng quát của Hà Nội
        if (lat >= 20.5 && lat <= 21.4 && lng >= 105.2 && lng <= 106.1) {
            return "Hà Nội";
        }

        return null;
    }

    /**
     * Thuật toán Ray Casting để kiểm tra điểm nằm trong Polygon
     */
    private boolean isPointInPolygon(double lat, double lng, List<Point> polygon) {
        boolean isInside = false;
        int n = polygon.size();
        for (int i = 0, j = n - 1; i < n; j = i++) {
            if (((polygon.get(i).lng > lng) != (polygon.get(j).lng > lng)) &&
                (lat < (polygon.get(j).lat - polygon.get(i).lat) * (lng - polygon.get(i).lng) / (polygon.get(j).lng - polygon.get(i).lng) + polygon.get(i).lat)) {
                isInside = !isInside;
            }
        }
        return isInside;
    }
}
