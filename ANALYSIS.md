# Phân Tích Dự Án Trip4HaNoi-BE

## Tổng Quan

**Trip4HaNoi** là một ứng dụng backend du lịch thông minh dành cho thành phố Hà Nội, được xây dựng bằng **Spring Boot 4.0.3** với Java 21. Định vị sản phẩm là **"Người bạn bản địa số"** — giúp du khách lên lịch trình thông minh, cá nhân hóa dựa trên AI và dữ liệu văn hóa địa phương.

---

## 1. Tech Stack

| Thành phần | Công nghệ |
|---|---|
| Framework | Spring Boot 4.0.3 |
| Java | Java 21 |
| Database | MySQL (localhost:3306/trip4hanoi) |
| ORM | Spring Data JPA + Hibernate |
| Mapping | MapStruct 1.6.3 |
| Boilerplate | Lombok |
| HTTP Client | Spring WebFlux (WebClient) |
| AI Integration | Google Gemini API (gemini-flash-latest) |
| Build | Maven |
| Port | 8080 |

> **[WARNING]**
> `spring.jpa.hibernate.ddl-auto=create` — Cấu hình này sẽ **XÓA và TẠO LẠI toàn bộ bảng** mỗi lần khởi động! Cực kỳ nguy hiểm khi deploy production. Nên đổi sang `validate` hoặc `update`.

> **[CAUTION]**
> **API Key Gemini bị lộ** trong `application.properties` — Cần chuyển sang biến môi trường ngay lập tức!

---

## 2. Kiến Trúc Package

```
com.trip4hanoi.app/
├── Trip4HaNoiBeApplication.java   # Entry point
├── common/                         # Enums dùng chung
│   ├── AuthProvider.java
│   ├── Gender.java
│   └── UserStatus.java
├── config/                         # Cấu hình ứng dụng
│   ├── DataInitializer.java        # Seed data khi khởi động
│   └── GeminiConfig.java           # Config WebClient cho Gemini
├── controller/                     # REST API endpoints
│   ├── AuthController.java
│   ├── CategoryController.java
│   ├── ChatController.java
│   ├── EventController.java
│   ├── ItineraryController.java
│   └── PlaceController.java
├── dto/                            # Data Transfer Objects
│   ├── req/                        # 13 Request DTOs
│   └── res/                        # 21 Response DTOs
├── entity/                         # 18 JPA Entities
├── exception/                      # Xử lý lỗi toàn cục
├── mapper/                         # 15 MapStruct mappers
├── repository/                     # 10 Spring Data repos
└── service/                        # Business logic
    ├── (6 interfaces)
    └── impl/ (6 implementations)
```

---

## 3. Domain Model (Entity)

### Các Entity chính (18 bảng)

```
User ──< Itinerary ──< ItineraryPlace >── Place >── Category
User ──< UserPreference
User ──< Review >── Place
User ──< Post >──< Comment
User ──< Post >──< PostImage
User ──< Post >──< PostLike
User ──< Post >──< PostSave
User ──< Notification
User ──< SavedPlace >── Place
User ──< UserEventFollow >── Event >── Place
User >──< Role
```

### Mô tả User Entity (quan trọng nhất)
- Auth đa nhà cung cấp: `AuthProvider` (local/OAuth)
- Trạng thái: `UserStatus` + `verificationCode` (email OTP?)
- Phân quyền: Many-to-Many với `Role` → `Permission`
- Có đầy đủ: itineraries, preferences, reviews, posts, comments, notifications, savedPlaces, postLikes, postSaves

---

## 4. API Endpoints

### `/api/auth`
| Method | Path | Mô tả |
|---|---|---|
| POST | `/login` | Đăng nhập (trả token?) |

### `/api/itineraries`
| Method | Path | Mô tả |
|---|---|---|
| POST | `/create` | Tạo lịch trình thông minh |
| POST | `/add-place` | Thêm địa điểm vào lịch trình |
| GET | `/my` | Lấy lịch trình của user |

### `/api/events`
| Method | Path | Mô tả |
|---|---|---|
| GET | `/` | Lấy tất cả sự kiện |
| POST | `/follow` | Theo dõi sự kiện |

### `/api/places` (PlaceController)
### `/api/categories` (CategoryController)
### `/api/chat` (ChatController — AI)

> **[NOTE]** Tất cả API đều trả về `APIResponse<T>` wrapper chuẩn với `status`, `code`, `message`, `data`.

---

## 5. Tính Năng Cốt Lõi

### 5.1. AI Chat (GeminiServiceImpl)
- Nhận câu hỏi từ người dùng
- Load toàn bộ địa điểm từ DB làm context
- Gửi prompt structured đến **Gemini Flash** API
- Parse response JSON trả về `ChatResponse` gồm: `introduction`, `timeline[]`, `summary`, `suggestedPlaceIds[]`
- Sử dụng `WebClient` (reactive) nhưng gọi `.block()` → thực chất là synchronous

### 5.2. Smart Itinerary Generator (ItineraryServiceImpl) ⭐
Đây là tính năng **phức tạp và thú vị nhất** của dự án:

**Input:** `title`, `budget`, `days`, `numberOfPeople`, `categoryNames[]`

**Thuật toán lập lịch:**
1. Chia ngân sách theo ngày và theo buổi (Morning 21.6%, Noon 21.6%, Afternoon 21.6%, Evening 35%)
2. Mỗi ngày có 4 buổi: Morning / Noon / Afternoon / Evening
3. Mỗi buổi có 2-3 địa điểm tùy số người
4. Áp dụng **slot targeting** — mỗi slot trong buổi có loại địa điểm ưu tiên:
   - Morning slot 0: Ẩm thực (Phở, Bánh mì...) → slot 1: Di tích/Lịch sử
   - Noon slot 0: Ăn trưa → slot 1: Cafe/Trà
   - Afternoon slot 0: Tham quan → slot 1: Workshop/Nghệ thuật
   - Evening slot 0: Ăn tối → slot 1: Giải trí/Bar
5. **Scoring system** (0-1):
   - Preference match: 40%
   - Rating: 30%
   - Budget fit: 30%
6. Lấy top 5 địa điểm → random chọn 1 (để tránh lịch trình giống nhau)
7. Đảm bảo không lặp địa điểm (`usedPlaces`)

**Upsert logic:** Nếu đã tồn tại lịch trình cùng tên → xóa cũ và tạo lại.

### 5.3. Data Seeding
`DataInitializer` seed dữ liệu mẫu khi DB trống:
- **115 địa điểm** thực tế tại Hà Nội (tên thật, không random)
- 5 categories: Ẩm thực, Cafe, Rạp chiếu phim, Địa điểm du lịch, Workshop
- 2 user mẫu + preferences

---

## 6. Vấn Đề & Điểm Cần Cải Thiện

### 🔴 Nghiêm trọng (Critical)

| # | Vấn đề | Chi tiết |
|---|---|---|
| 1 | **API Key lộ thiên** | `gemini.api.key` hardcode trong `application.properties` và commit lên Git |
| 2 | **DDL auto=create** | Mỗi lần restart sẽ drop toàn bộ database |
| 3 | **Mật khẩu plain text** | `DataInitializer` lưu password `"123456"` không mã hóa |
| 4 | **Không có Spring Security** | `application.properties` có config Security nhưng `pom.xml` không có dependency `spring-boot-starter-security` |
| 5 | **Không có JWT** | `AuthService` login trả `AuthResponse` nhưng không rõ có token thật không |

### 🟡 Vấn đề thiết kế (Design Issues)

| # | Vấn đề | Chi tiết |
|---|---|---|
| 6 | **Sai ErrorCode** | `addPlaceToItinerary` throw `POST_NOT_FOUND` khi không tìm thấy Itinerary/Place |
| 7 | **placeRepository.findAll()** | Load toàn bộ địa điểm vào RAM mỗi lần tạo lịch trình — không scalable |
| 8 | **`@Operation` thiếu import** | `EventController.java` dùng `@Operation` nhưng không có OpenAPI/Swagger dependency |
| 9 | **`DataInitializer` bị comment 2 lần** | File có code commented và code thật cùng tồn tại — rất khó đọc |
| 10 | **`User` thiếu `@EqualsAndHashCode`** | `@Data` + entity Hibernate có thể gây vấn đề với circular reference |

### 🟢 Tính năng còn thiếu (theo PLAN.md)

| Tính năng | Trạng thái |
|---|---|
| Spring Security + JWT | ❌ Chưa có |
| Redis caching | ❌ Chưa có |
| Google Maps API | ❌ Chưa có |
| OAuth2 (Google login) | ❌ Chưa có (có `AuthProvider` enum nhưng chưa implement) |
| Notification system | ❌ Entity có, service/controller chưa có |
| Post/Community feature | ❌ Entity có, chưa có controller |
| VNPay/Momo payment | ❌ Chưa có |
| Email verification | ❌ `verificationCode` có, service chưa implement |

---

## 7. Điểm Mạnh

✅ **Kiến trúc rõ ràng** — Đúng pattern Controller → Service → Repository  
✅ **MapStruct** — Mapping entity↔DTO không dùng tay  
✅ **DataInitializer** — Dữ liệu seed với địa điểm thực tế Hà Nội  
✅ **Thuật toán lịch trình** — Logic session-based với scoring phức tạp, có tư duy nghiệp vụ  
✅ **APIResponse wrapper** — Format response nhất quán  
✅ **Gemini AI integration** — Đã có working chatbot với context từ DB  
✅ **Java 21 + Spring Boot 4** — Stack mới nhất  

---

## 8. Đề Xuất Bước Tiếp Theo (Ưu tiên cao → thấp)

```
1. [KHẨN] Chuyển gemini.api.key sang biến môi trường (.env / System.getenv)
2. [KHẨN] Đổi ddl-auto=create → update (hoặc dùng Flyway/Liquibase)
3. [CAO]   Thêm Spring Security + JWT (spring-boot-starter-security)
4. [CAO]   Mã hóa password bằng BCryptPasswordEncoder
5. [CAO]   Thêm Spring Validation (@Valid, @NotBlank...) cho các Request DTO
6. [TRUNG] Phân trang (Pageable) cho các API list places/events
7. [TRUNG] Fix ErrorCode sai (dùng ITINERARY_NOT_FOUND, PLACE_NOT_FOUND)
8. [TRUNG] Thêm Swagger/OpenAPI (springdoc-openapi-starter-webmvc-ui)
9. [THẤP]  Tối ưu itinerary generator: query places theo category thay vì findAll()
10. [THẤP] Implement Post/Comment/Notification controllers
```
