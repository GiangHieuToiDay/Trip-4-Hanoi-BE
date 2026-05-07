package com.trip4hanoi.app.config;

import com.trip4hanoi.app.common.AuthProvider;
import com.trip4hanoi.app.entity.*;
import com.trip4hanoi.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PlaceRepository placeRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public void run(String... args) {

        if (placeRepository.count() > 0) return;

        Category food = getOrCreateCategory("Ẩm thực");
        Category cafe = getOrCreateCategory("Cafe");
        Category cinema = getOrCreateCategory("Rạp chiếu phim");
        Category travel = getOrCreateCategory("Địa điểm du lịch");
        Category workshop = getOrCreateCategory("Workshop");

        List<Place> all = new ArrayList<>();

        // ===== FOOD (30) =====
        String[] foods = {
                "Phở Thìn Bờ Hồ","Phở Bát Đàn","Phở 10 Lý Quốc Sư","Bún chả Hương Liên","Bún chả Đắc Kim",
                "Bún đậu Phất Lộc","Bún đậu Trung Hương","Chả cá Lã Vọng","Chả cá Anh Vũ","Miến lươn Đông Thịnh",
                "Miến trộn Hàng Bạc","Xôi Yến","Bánh cuốn Thanh Vân","Bánh cuốn Bà Hoành","Bánh mì 25",
                "Bánh mì Nguyên Sinh","Bún riêu Hàng Bạc","Bún cá Sâm Cây Si","Bún thang Bà Đức",
                "Bún mọc Hàng Lược","Nem rán Hàng Bồ","Ốc luộc Hàng Chai","Lẩu Phan","Hotpot Story",
                "King BBQ","Sumo BBQ","Kichi Kichi","Sen Tây Hồ","Poseidon Buffet","Quán ăn Ngon"
        };
        addPlaces(all, foods, "Trải nghiệm ẩm thực truyền thống Hà Nội", 60000, 4.5, food);

        // ===== CAFE (30) =====
        String[] cafes = {
                "Cộng Cà Phê","The Note Coffee","Trill Rooftop","Highlands Coffee","Aha Coffee",
                "The Coffee House","Starbucks Hanoi","Loading T Cafe","Lofita","Xofa Cafe",
                "Blackbird Coffee","Kone Cafe","Serein Cafe","Vintage 1976","Ohi Tree",
                "Eden Coffee","Laika Cafe","Trixie Cafe","An Cafe","Work Cafe",
                "Hanoi Social Club","Manzi","Maison de Tet","Cup of Tea","Rustics Coffee",
                "Cafe Giảng","Cafe Đinh","Hidden Gem","Timeline Coffee","Tiny Cafe"
        };
        addPlaces(all, cafes, "Không gian cafe cực chill tại Hà Thành", 45000, 4.4, cafe);

        // ===== WORKSHOP (20) =====
        String[] workshops = {
                "Bát Tràng Pottery","Toong Workshop","Creative City Workshop","Art Tree",
                "Craft Link","Tired City","Work Room Four","Heritage Space",
                "Manzi Workshop","Vẽ tranh Canvas","Làm nến thơm","DIY Workshop",
                "Gốm Bát Tràng Experience","Vẽ màu nước","Workshop làm bánh",
                "Workshop cắm hoa","Workshop xà phòng","Workshop thư pháp",
                "Workshop handmade","Workshop resin"
        };
        addPlaces(all, workshops, "Workshop trải nghiệm sáng tạo", 300000, 4.6, workshop);

        // ===== MUSEUM (10) =====
        String[] museums = {
                "Bảo tàng Dân tộc học","Bảo tàng Hồ Chí Minh","Bảo tàng Lịch sử",
                "Bảo tàng Mỹ thuật","Bảo tàng Phụ nữ","Bảo tàng Hà Nội",
                "Bảo tàng Quân đội","Bảo tàng Công an","Bảo tàng B52","Bảo tàng Địa chất"
        };
        addPlaces(all, museums, "Tìm hiểu lịch sử và văn hóa", 40000, 4.7, travel);

        // ===== HISTORICAL (10) =====
        String[] historical = {
                "Văn Miếu","Hoàng thành Thăng Long","Nhà tù Hỏa Lò","Chùa Một Cột",
                "Chùa Trấn Quốc","Đền Ngọc Sơn","Đền Quán Thánh","Phủ Tây Hồ",
                "Cột cờ Hà Nội","Nhà thờ Lớn"
        };
        addPlaces(all, historical, "Di tích lịch sử nghìn năm văn hiến", 30000, 4.8, travel);

        // ===== CINEMA (15) =====
        String[] cinemas = {
                "CGV Bà Triệu","CGV Royal City","CGV Times City","CGV Tràng Tiền",
                "Lotte Liễu Giai","Lotte Hà Đông","BHD Phạm Ngọc Thạch",
                "BHD Vincom","Galaxy Mipec","Beta Mỹ Đình",
                "Beta Thanh Xuân","Cinestar","Mega GS","Platinum","National Cinema"
        };
        addPlaces(all, cinemas, "Giải trí đỉnh cao với những bộ phim bom tấn", 120000, 4.5, cinema);

        placeRepository.saveAll(all);

        // ===== USER + PREFERENCES =====
        if (userRepository.count() == 0) {

            User user1 = userRepository.save(User.builder()
                    .name("Hải Nam")
                    .email("hainam@gmail.com")
                    .password("123456")
                    .nationality("Vietnam")
                    .provider(AuthProvider.LOCAL)
                    .language("vi")
                    .build());

            User user2 = userRepository.save(User.builder()
                    .name("John Doe")
                    .email("johndoe@gmail.com")
                    .password("123456")
                    .nationality("USA")
                    .provider(AuthProvider.LOCAL)
                    .language("en")
                    .build());

            // User 1 thích cafe + ăn uống
            userPreferenceRepository.save(UserPreference.builder().user(user1).category(cafe).build());
            userPreferenceRepository.save(UserPreference.builder().user(user1).category(food).build());

            // User 2 thích du lịch + workshop
            userPreferenceRepository.save(UserPreference.builder().user(user2).category(travel).build());
            userPreferenceRepository.save(UserPreference.builder().user(user2).category(workshop).build());
        }

        System.out.println(">>> LOAD FULL DATA + USER + PREFERENCES SUCCESS!");


        //event
        if(eventRepository.count() == 0) {
            //lấy 1 vài địa điểm tiêu biểu để gắn event
            Place vanMieu = placeRepository.findByName("Văn Miếu").orElse(null);
            Place baoTangMyThuat = placeRepository.findByName("Bảo tàng Mỹ Thuật").orElse(null);
            Place phoDiBo = placeRepository.findByName("Đền Ngọc Sơn").orElse(null);

            if(vanMieu != null) {
                eventRepository.save(Event.builder()
                     .name("Lễ hội Chữ Xuân 2026")
                     .description("Hoạt động xin chữ đầu năm và triển lãm thư pháp đặc sắc.")
                     .place(vanMieu)
                     .startTime(LocalDateTime.of(2026, 5, 1, 8, 0))
                     .endTime(LocalDateTime.of(2026, 5, 10, 18, 0))
                     .build());
            }

            if (baoTangMyThuat != null) {
                eventRepository.save(Event.builder().name("Triển lãm Sơn mài Hiện đại")
                                .description("Trưng bày hơn 50 tác phẩm sơn mài từ các nghệ sĩ trẻ.")
                                .place(baoTangMyThuat)
                                .startTime(LocalDateTime.of(2026, 5, 5, 9, 0))
                                .endTime(LocalDateTime.of(2026, 5, 15, 17, 0))
                        .build());
            }

            System.out.println(">>> SEED EVENTS SUCCESS!");

        }


    }

    private void addPlaces(List<Place> list, String[] names, String desc, int price, double rate, Category c) {
        for (String name : names) {
            list.add(place(name, desc, "Hà Nội", price, rate, c));
        }
    }

    private Category getOrCreateCategory(String name) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> categoryRepository.save(Category.builder().name(name).build()));
    }

    private Place place(String name, String desc, String address, int price, double rate, Category c) {
        // Tọa độ gốc mặc định (Hoàn Kiếm)
        double lat = 21.0285;
        double lng = 105.8527;
        String district = "Hoàn Kiếm";

        // Logic gán Quận và Tọa độ theo từ khóa thông minh
        if (name.contains("Tây Hồ") || name.contains("Trấn Quốc") || name.contains("Phủ Tây Hồ")) {
            lat = 21.0664; lng = 105.8277; district = "Tây Hồ";
        } else if (name.contains("Cầu Giấy") || name.contains("Dân tộc học") || name.contains("BHD Vincom")) {
            lat = 21.0362; lng = 105.7905; district = "Cầu Giấy";
        } else if (name.contains("Ba Đình") || name.contains("Liễu Giai") || name.contains("Lotte") || name.contains("Hồ Chí Minh") || name.contains("Một Cột") || name.contains("Quân đội")) {
            lat = 21.0358; lng = 105.8335; district = "Ba Đình";
        } else if (name.contains("Thanh Xuân") || name.contains("Royal City")) {
            lat = 21.0028; lng = 105.7951; district = "Thanh Xuân";
        } else if (name.contains("Đống Đa") || name.contains("Văn Miếu") || name.contains("Hàng Cháo") || name.contains("Phạm Ngọc Thạch")) {
            lat = 21.0294; lng = 105.8361; district = "Đống Đa";
        } else if (name.contains("Hai Bà Trưng") || name.contains("Bà Triệu") || name.contains("Times City") || name.contains("Tràng Tiền")) {
            lat = 21.0125; lng = 105.8494; district = "Hai Bà Trưng";
        }

        // Thêm độ lệch ngẫu nhiên nhỏ (trong khoảng +/- 500m) để các điểm không bị trùng khít
        lat += (Math.random() * 0.01 - 0.005);
        lng += (Math.random() * 0.01 - 0.005);

        // Tạo ảnh giả lập dựa trên tên để mỗi địa điểm có ảnh riêng
        String imageSeed = name.replaceAll("\\s+", "");
        String imageUrl = "https://picsum.photos/seed/" + imageSeed + "/800/600";

        Place place = Place.builder()
                .name(name)
                .description(desc)
                .address(address + ", " + district)
                .district(district)
                .latitude(lat)
                .longitude(lng)
                .priceAvg(price)
                .ratingAvg(rate)
                .viewCount((int) (Math.random() * 1000)) // Giả lập lượt xem từ 0-1000
                .favoriteCount((int) (Math.random() * 200)) // Giả lập yêu thích từ 0-200
                .images(new ArrayList<>())
                .category(c)
                .build();
                
        place.getImages().add(PlaceImage.builder()
                .imageUrl(imageUrl)
                .publicId("dummy_" + imageSeed)
                .place(place)
                .build());
                
        return place;
    }


}
