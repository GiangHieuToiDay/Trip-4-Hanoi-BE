package com.trip4hanoi.app.config;

import com.trip4hanoi.app.common.AuthProvider;
import com.trip4hanoi.app.common.UserStatus;
import com.trip4hanoi.app.entity.*;
import com.trip4hanoi.app.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "DATA INITIALIZER")
public class DataInitializer implements CommandLineRunner {

    private final PlaceRepository placeRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final EventRepository eventRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserLocationHistoryRepository userLocationHistoryRepository;

    @Value("${app.init.admin-password:123456}")
    private String adminPassword;

    @Value("${app.init.staff-password:123456}")
    private String staffPassword;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting Data Initialization...");


        createPermissions();
        createRoles();
        createAccounts();

        // Khởi tạo dữ liệu Du lịch (Nếu chưa có)
        if (placeRepository.count() == 0) {
            createTravelData();
        }

        // Khởi tạo User Preference cho user@gmail.com để test isRecommended
        initUserPreferences();

        // Khởi tạo dữ liệu Test chuyên sâu cho Itinerary Logic
        initTestData();

        log.info("Data Initialization Completed Successfully!");
    }

    private void initTestData() {
        String testEmail = "tester@gmail.com";
        if (userRepository.existsByEmail(testEmail)) return;

        log.info("Initializing specialized test data for Itinerary Logic...");

        // 1. Tạo User Test
        Role userRole = roleRepository.findByName("USER").orElseThrow();
        User tester = User.builder()
                .username("SmartTester")
                .email(testEmail)
                .password(passwordEncoder.encode("123456"))
                .roles(Set.of(userRole))
                .provider(AuthProvider.LOCAL)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(tester);

        // 2. Thiết lập sở thích: Du lịch và Cafe
        Category travel = getOrCreateCategory("Địa điểm du lịch");
        Category cafe = getOrCreateCategory("Cafe");
        Category food = getOrCreateCategory("Ẩm thực");

        userPreferenceRepository.save(UserPreference.builder().user(tester).category(travel).build());
        userPreferenceRepository.save(UserPreference.builder().user(tester).category(cafe).build());

        // 3. Giả lập vị trí: User đang ở Cầu Giấy (Tòa nhà Keangnam)
        // Lat: 21.0173, Lng: 105.7841
        userLocationHistoryRepository.save(UserLocationHistory.builder()
                .user(tester)
                .latitude(21.0173)
                .longitude(105.7841)
                .district("Cầu Giấy")
                .actionType("BACKGROUND")
                .createdAt(LocalDateTime.now())
                .build());

        // 4. Tạo địa điểm ở Cầu Giấy (Gần User - Sẽ có điểm Distance cao)
        List<Place> testPlaces = new ArrayList<>();
        
        // Cafe gần Keangnam (Cầu Giấy)
        testPlaces.add(createTestPlace("Highlands Keangnam", "Cafe gần chỗ ở", 21.0175, 105.7845, "Cầu Giấy", cafe, 4.5));
        // Công viên Cầu Giấy (Du lịch gần đó)
        testPlaces.add(createTestPlace("Công viên Cầu Giấy", "Điểm dạo mát gần User", 21.0205, 105.7915, "Cầu Giấy", travel, 4.7));
        // Nhà hàng ở Cầu Giấy
        testPlaces.add(createTestPlace("Cơm Niêu Thúy Nga", "Ăn trưa gần Keangnam", 21.0255, 105.8015, "Cầu Giấy", food, 4.2));

        // 5. Tạo địa điểm ở Hoàn Kiếm (Xa User - Sẽ có điểm Distance thấp)
        testPlaces.add(createTestPlace("Kem Tràng Tiền", "Rất xa Cầu Giấy", 21.0245, 105.8545, "Hoàn Kiếm", food, 4.8));
        testPlaces.add(createTestPlace("Nhà Thờ Lớn", "Điểm tham quan xa", 21.0285, 105.8495, "Hoàn Kiếm", travel, 4.9));

        placeRepository.saveAll(testPlaces);
        log.info("Specialized test data initialized for tester@gmail.com");
    }

    private Place createTestPlace(String name, String desc, double lat, double lng, String dist, Category cat, double rate) {
        Place p = Place.builder()
                .name(name)
                .description(desc)
                .address(dist + ", Hà Nội")
                .district(dist)
                .latitude(lat)
                .longitude(lng)
                .priceAvg(50000)
                .ratingAvg(rate)
                .category(cat)
                .images(new ArrayList<>())
                .build();
        
        p.getImages().add(PlaceImage.builder()
                .imageUrl("https://picsum.photos/800/600?random=" + name.hashCode())
                .publicId("test_" + UUID.randomUUID())
                .place(p)
                .build());
        return p;
    }

    private void initUserPreferences() {
        userRepository.findByEmail("user@gmail.com").ifPresent(user -> {
            if (userPreferenceRepository.findByUserId(user.getId()).isEmpty()) {
                categoryRepository.findByName("Ẩm thực").ifPresent(cat -> {
                    userPreferenceRepository.save(UserPreference.builder()
                            .user(user)
                            .category(cat)
                            .build());
                    log.info("Set 'Ẩm thực' as preference for user@gmail.com");
                });
            }
        });
    }

    private void createPermissions() {
        // Administrative & System
        createPermissionIfNotExist("MANAGE_USER", "Quản trị người dùng (Xem/Sửa/Khóa)");
        createPermissionIfNotExist("MANAGE_ROLE", "Quản trị vai trò và quyền hạn");

        //Category
        createPermissionIfNotExist("MANAGE_CATEGORY", "Quản trị địa điểm và danh mục");
        // Content & Operation
        createPermissionIfNotExist("MANAGE_PLACE", "Quản trị địa điểm và danh mục");
        createPermissionIfNotExist("MANAGE_EVENT", "Quản trị sự kiện du lịch");
        createPermissionIfNotExist("MODERATE_CONTENT", "Kiểm duyệt bài đăng, bình luận, đánh giá");

        // Basic Access
        createPermissionIfNotExist("VIEW_ALL", "Xem thông tin hệ thống (Công khai)");
    }

    private void createPermissionIfNotExist(String name, String description) {
        if (!permissionRepository.existsByName(name)) {
            permissionRepository.save(Permission.builder()
                    .name(name)
                    .description(description)
                    .build());
        }
    }

    private void createRoles() {
        List<Permission> allPermissions = permissionRepository.findAll();

        // ADMIN: Toàn quyền
        Set<Permission> adminPermissions = new HashSet<>(allPermissions);
        createRoleIfNotExist("ADMIN", "Quản trị viên hệ thống", adminPermissions);

        // STAFF: Quản lý nội dung và kiểm duyệt cộng đồng
        Set<String> staffPermissionNames = Set.of(
                "MANAGE_PLACE", "MANAGE_EVENT", "MODERATE_CONTENT", "VIEW_ALL","MANAGE_CATEGORY"
        );
        Set<Permission> staffPermissions = allPermissions.stream()
                .filter(p -> staffPermissionNames.contains(p.getName()))
                .collect(Collectors.toSet());
        createRoleIfNotExist("STAFF", "Nhân viên vận hành nội dung", staffPermissions);

        // USER: Chỉ xem và quyền cá nhân (Quyền cá nhân xử lý qua Ownership)
        Set<String> userPermissionNames = Set.of("VIEW_ALL");
        Set<Permission> userPermissions = allPermissions.stream()
                .filter(p -> userPermissionNames.contains(p.getName()))
                .collect(Collectors.toSet());
        createRoleIfNotExist("USER", "Người dùng ứng dụng", userPermissions);
    }

    private void createRoleIfNotExist(String name, String description, Set<Permission> permissions) {
        roleRepository.findByName(name).ifPresentOrElse(
            role -> {
                role.setPermissions(permissions);
                roleRepository.save(role);
            },
            () -> {
                Role newRole = Role.builder()
                        .name(name)
                        .description(description)
                        .permissions(permissions)
                        .build();
                roleRepository.save(newRole);
            }
        );
    }

    private void createAccounts() {
        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
        Role staffRole = roleRepository.findByName("STAFF").orElseThrow();
        Role userRole = roleRepository.findByName("USER").orElseThrow();

        createDefaultAccount("HanoiAdmin", "admin@trip4hanoi.com", adminPassword, Set.of(adminRole));
        createDefaultAccount("HanoiStaff", "staff@trip4hanoi.com", staffPassword, Set.of(staffRole));
        createDefaultAccount("HanoiUser", "user@gmail.com", "123456", Set.of(userRole));
    }

    private void createDefaultAccount(String username, String email, String password, Set<Role> roles) {
        if (!userRepository.existsByEmail(email)) {
            User user = User.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .roles(roles)
                    .provider(AuthProvider.LOCAL)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(user);
            log.info("Created default account: {} / {}", email, password);
        }
    }

    private void createTravelData() {
        Category food = getOrCreateCategory("Ẩm thực");
        Category cafe = getOrCreateCategory("Cafe");
        Category travel = getOrCreateCategory("Địa điểm du lịch");
        Category workshop = getOrCreateCategory("Workshop");
        Category cinema = getOrCreateCategory("Rạp chiếu phim");

        List<Place> all = new ArrayList<>();

        // Ẩm thực (Mẫu)
        addPlaces(all, new String[]{"Phở Thìn Bờ Hồ", "Phở Bát Đàn", "Bún chả Hương Liên"}, "Trải nghiệm ẩm thực truyền thống", 60000, 4.5, food);
        // Cafe (Mẫu)
        addPlaces(all, new String[]{"Cộng Cà Phê", "The Note Coffee", "Cafe Giảng"}, "Không gian cafe cực chill", 45000, 4.4, cafe);

        // Thêm dữ liệu ở Quận khác (Tây Hồ) để test Hot Zone
        addPlacesInDistrict(all, new String[]{"Sen Tây Hồ", "Nhà hàng 6 Degrees"}, "View Hồ Tây cực đẹp", 150000, 4.8, food, "Tây Hồ");

        placeRepository.saveAll(all);

        // Events
        Place phoThin = placeRepository.findByName("Phở Thìn Bờ Hồ").orElse(null);
        if (phoThin != null) {
            eventRepository.save(Event.builder()
                    .name("Lễ hội Ẩm thực Hà Nội 2026")
                    .description("Sự kiện hội tụ các tinh hoa ẩm thực đường phố.")
                    .place(phoThin)
                    .startTime(LocalDateTime.now().minusDays(1)) // Bắt đầu từ hôm qua
                    .endTime(LocalDateTime.now().plusDays(7))   // Kết thúc sau 7 ngày
                    .build());
            log.info("Created active event for 'Phở Thìn Bờ Hồ'");
        }

        log.info("Travel data seeded success!");
    }

    private Category getOrCreateCategory(String name) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> categoryRepository.save(Category.builder().name(name).build()));
    }

    private void addPlaces(List<Place> list, String[] names, String desc, int price, double rate, Category c) {
        for (String name : names) {
            Place p = Place.builder()
                    .name(name)
                    .description(desc)
                    .address("Hà Nội")
                    .district("Hoàn Kiếm")
                    .latitude(21.0285 + (Math.random() * 0.01))
                    .longitude(105.8527 + (Math.random() * 0.01))
                    .priceAvg(price)
                    .ratingAvg(rate)
                    .category(c)
                    .images(new ArrayList<>())
                    .build();


            String dummyPublicId = "dummy_" + UUID.randomUUID().toString().substring(0, 8);
            p.getImages().add(PlaceImage.builder()
                    .imageUrl("https://picsum.photos/800/600")
                    .publicId(dummyPublicId)
                    .place(p)
                    .build());
            list.add(p);
        }
    }

    private void addPlacesInDistrict(List<Place> list, String[] names, String desc, int price, double rate, Category c, String district) {
        for (String name : names) {
            Place p = Place.builder()
                    .name(name)
                    .description(desc)
                    .address("Hà Nội")
                    .district(district)
                    .latitude(21.0585 + (Math.random() * 0.01))
                    .longitude(105.8227 + (Math.random() * 0.01))
                    .priceAvg(price)
                    .ratingAvg(rate)
                    .category(c)
                    .images(new ArrayList<>())
                    .build();

            String dummyPublicId = "dummy_" + UUID.randomUUID().toString().substring(0, 8);
            p.getImages().add(PlaceImage.builder()
                    .imageUrl("https://picsum.photos/800/600")
                    .publicId(dummyPublicId)
                    .place(p)
                    .build());
            list.add(p);
        }
    }

}
