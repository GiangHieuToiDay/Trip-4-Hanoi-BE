// package com.trip4hanoi.app.config;

// import com.trip4hanoi.app.entity.Category;
// import com.trip4hanoi.app.entity.Place;
// import com.trip4hanoi.app.entity.User;
// import com.trip4hanoi.app.entity.UserPreference;
// import com.trip4hanoi.app.repository.CategoryRepository;
// import com.trip4hanoi.app.repository.PlaceRepository;
// import com.trip4hanoi.app.repository.UserPreferenceRepository;
// import com.trip4hanoi.app.repository.UserRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.Random;

// @Component
// @RequiredArgsConstructor
// public class DataInitializer implements CommandLineRunner {

//     private final PlaceRepository placeRepository;
//     private final CategoryRepository categoryRepository;
//     private final UserRepository userRepository;
//     private final UserPreferenceRepository userPreferenceRepository;
//     private final Random random = new Random();

//     @Override
//     @Transactional
//     public void run(String... args) throws Exception {
//         // Nạp Category nếu chưa có
//         if (categoryRepository.count() < 7) {
//             Category food = getOrCreateCategory("Ẩm thực");
//             Category cafe = getOrCreateCategory("Cafe");
//             Category cinema = getOrCreateCategory("Rạp chiếu phim");
//             Category travel = getOrCreateCategory("Địa điểm du lịch");
//             Category workshop = getOrCreateCategory("Workshop");
//             Category photobooth = getOrCreateCategory("Photobooth");
//             Category homestay = getOrCreateCategory("Homestay");

//             // Nạp 200 địa điểm
//             List<Place> allPlaces = new ArrayList<>();
//             allPlaces.addAll(generatePlaces("Quán ăn", food, 30, List.of("Phở", "Bún chả", "Bún đậu", "Chả cá"), 50000, 200000));
//             allPlaces.addAll(generatePlaces("Cafe", cafe, 30, List.of("Aha Coffee", "Highlands", "Cộng Cà Phê", "The Coffee House"), 30000, 80000));
//             allPlaces.addAll(generatePlaces("Rạp", cinema, 30, List.of("CGV Cinema", "Lotte Cinema", "BHD Star"), 80000, 150000));
//             allPlaces.addAll(generatePlaces("Điểm du lịch", travel, 30, List.of("Công viên", "Bảo tàng", "Hồ", "Di tích"), 0, 50000));
//             allPlaces.addAll(generatePlaces("Workshop", workshop, 20, List.of("Làm gốm", "Vẽ tranh", "Làm nến thơm"), 150000, 500000));
//             allPlaces.addAll(generatePlaces("Photobooth", photobooth, 30, List.of("Life4Cut", "PhotoTime", "Haru Film"), 50000, 120000));
//             allPlaces.addAll(generatePlaces("Homestay", homestay, 30, List.of("Hanoi Cozy", "Old Quarter Home", "Lakeside View"), 400000, 1500000));
//             placeRepository.saveAll(allPlaces);
//             System.out.println(">>> ĐÃ NẠP THÀNH CÔNG 200 ĐỊA ĐIỂM!");
//         }

//         // Nạp 2 User mẫu nếu chưa có
//         if (userRepository.count() == 0) {
//             User user1 = userRepository.save(User.builder()
//                     .name("Hải Nam")
//                     .email("hainam@gmail.com")
//                     .password("123456") // Lưu ý: Nếu có Security thì nên dùng BCrypt
//                     .nationality("Vietnam")
//                     .language("vi")
//                     .build());

//             User user2 = userRepository.save(User.builder()
//                     .name("John Doe")
//                     .email("johndoe@gmail.com")
//                     .password("123456")
//                     .nationality("USA")
//                     .language("en")
//                     .build());

//             // Nạp thêm sở thích cho User để AI biết đường tư vấn
//             Category cafeCat = categoryRepository.findByName("Cafe").orElse(null);
//             Category travelCat = categoryRepository.findByName("Địa điểm du lịch").orElse(null);
            
//             if (cafeCat != null) {
//                 userPreferenceRepository.save(UserPreference.builder().user(user1).category(cafeCat).build());
//             }
//             if (travelCat != null) {
//                 userPreferenceRepository.save(UserPreference.builder().user(user2).category(travelCat).build());
//             }

//             System.out.println(">>> ĐÃ NẠP THÀNH CÔNG 2 USER MẪU!");
//         }
//     }

//     private Category getOrCreateCategory(String name) {
//         return categoryRepository.findByName(name)
//                 .orElseGet(() -> categoryRepository.save(Category.builder().name(name).build()));
//     }

//     private List<Place> generatePlaces(String prefix, Category category, int count, List<String> samples, int minPrice, int maxPrice) {
//         List<Place> list = new ArrayList<>();
//         String[] districts = {"Hoàn Kiếm", "Ba Đình", "Tây Hồ", "Đống Đa", "Cầu Giấy", "Hai Bà Trưng"};
//         for (int i = 1; i <= count; i++) {
//             String baseName = samples.get(random.nextInt(samples.size()));
//             String district = districts[random.nextInt(districts.length)];
//             list.add(Place.builder()
//                     .name(baseName + " " + prefix + " " + i)
//                     .description("Địa điểm tuyệt vời tại " + district + " để trải nghiệm " + category.getName().toLowerCase() + ".")
//                     .address("Số " + (i * 3) + " Đường " + district + ", Hà Nội")
//                     .priceAvg(minPrice + random.nextInt(maxPrice - minPrice + 1))
//                     .ratingAvg(3.5 + (5.0 - 3.5) * random.nextDouble())
//                     .category(category)
//                     .build());
//         }
//         return list;
//     }
// }


package com.trip4hanoi.app.config;

import com.trip4hanoi.app.entity.Category;
import com.trip4hanoi.app.entity.Place;
import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.entity.UserPreference;
import com.trip4hanoi.app.repository.CategoryRepository;
import com.trip4hanoi.app.repository.PlaceRepository;
import com.trip4hanoi.app.repository.UserPreferenceRepository;
import com.trip4hanoi.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PlaceRepository placeRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;

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
        addPlaces(all, foods, "Quán ăn nổi tiếng", 50000, 4.5, food);

        // ===== CAFE (30) =====
        String[] cafes = {
                "Cộng Cà Phê","The Note Coffee","Trill Rooftop","Highlands Coffee","Aha Coffee",
                "The Coffee House","Starbucks Hanoi","Loading T Cafe","Lofita","Xofa Cafe",
                "Blackbird Coffee","Kone Cafe","Serein Cafe","Vintage 1976","Ohi Tree",
                "Eden Coffee","Laika Cafe","Trixie Cafe","An Cafe","Work Cafe",
                "Hanoi Social Club","Manzi","Maison de Tet","Cup of Tea","Rustics Coffee",
                "Cafe Giảng","Cafe Đinh","Hidden Gem","Timeline Coffee","Tiny Cafe"
        };
        addPlaces(all, cafes, "Quán cafe đẹp", 40000, 4.4, cafe);

        // ===== WORKSHOP (20) =====
        String[] workshops = {
                "Bát Tràng Pottery","Toong Workshop","Creative City Workshop","Art Tree",
                "Craft Link","Tired City","Work Room Four","Heritage Space",
                "Manzi Workshop","Vẽ tranh Canvas","Làm nến thơm","DIY Workshop",
                "Gốm Bát Tràng Experience","Vẽ màu nước","Workshop làm bánh",
                "Workshop cắm hoa","Workshop xà phòng","Workshop thư pháp",
                "Workshop handmade","Workshop resin"
        };
        addPlaces(all, workshops, "Workshop trải nghiệm", 250000, 4.6, workshop);

        // ===== MUSEUM (10) =====
        String[] museums = {
                "Bảo tàng Dân tộc học","Bảo tàng Hồ Chí Minh","Bảo tàng Lịch sử",
                "Bảo tàng Mỹ thuật","Bảo tàng Phụ nữ","Bảo tàng Hà Nội",
                "Bảo tàng Quân đội","Bảo tàng Công an","Bảo tàng B52","Bảo tàng Địa chất"
        };
        addPlaces(all, museums, "Bảo tàng", 30000, 4.7, travel);

        // ===== HISTORICAL (10) =====
        String[] historical = {
                "Văn Miếu","Hoàng thành Thăng Long","Nhà tù Hỏa Lò","Chùa Một Cột",
                "Chùa Trấn Quốc","Đền Ngọc Sơn","Đền Quán Thánh","Phủ Tây Hồ",
                "Cột cờ Hà Nội","Nhà thờ Lớn"
        };
        addPlaces(all, historical, "Di tích lịch sử", 30000, 4.8, travel);

        // ===== CINEMA (15) =====
        String[] cinemas = {
                "CGV Bà Triệu","CGV Royal City","CGV Times City","CGV Tràng Tiền",
                "Lotte Liễu Giai","Lotte Hà Đông","BHD Phạm Ngọc Thạch",
                "BHD Vincom","Galaxy Mipec","Beta Mỹ Đình",
                "Beta Thanh Xuân","Cinestar","Mega GS","Platinum","National Cinema"
        };
        addPlaces(all, cinemas, "Rạp chiếu phim", 100000, 4.5, cinema);

        placeRepository.saveAll(all);

        // ===== USER + PREFERENCES =====
        if (userRepository.count() == 0) {

            User user1 = userRepository.save(User.builder()
                    .name("Hải Nam")
                    .email("hainam@gmail.com")
                    .password("123456")
                    .nationality("Vietnam")
                    .language("vi")
                    .build());

            User user2 = userRepository.save(User.builder()
                    .name("John Doe")
                    .email("johndoe@gmail.com")
                    .password("123456")
                    .nationality("USA")
                    .language("en")
                    .build());

            // User 1 thích cafe + ăn uống
            userPreferenceRepository.save(UserPreference.builder()
                    .user(user1)
                    .category(cafe)
                    .build());

            userPreferenceRepository.save(UserPreference.builder()
                    .user(user1)
                    .category(food)
                    .build());

            // User 2 thích du lịch + workshop
            userPreferenceRepository.save(UserPreference.builder()
                    .user(user2)
                    .category(travel)
                    .build());

            userPreferenceRepository.save(UserPreference.builder()
                    .user(user2)
                    .category(workshop)
                    .build());
        }

        System.out.println(">>> LOAD FULL DATA + USER + PREFERENCES SUCCESS!");
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
        return Place.builder()
                .name(name)
                .description(desc)
                .address(address)
                .priceAvg(price)
                .ratingAvg(rate)
                .category(c)
                .build();
    }
}