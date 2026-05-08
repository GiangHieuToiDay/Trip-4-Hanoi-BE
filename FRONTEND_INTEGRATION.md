#  HƯỚNG DẪN TÍCH HỢP FRONTEND (TRIP4HANOI)

Tài liệu này tóm tắt cách Frontend (Web/Mobile) cần phối hợp với Backend để triển khai các tính năng thông minh: Định vị, Gợi ý, Lịch trình và Chat AI.

---

## 1. Quản lý Vị trí & Quyền riêng tư (Location & Privacy)
Đây là "nhiên liệu" cho toàn bộ thuật toán cá nhân hóa.

*   **Nhiệm vụ của FE:**
    1.  **Lấy tọa độ GPS:** Sử dụng thư viện gốc của thiết bị (Geolocator cho Mobile hoặc Navigator cho Web).
    2.  **Gửi tọa độ trong mọi yêu cầu:** Luôn đính kèm `userLat` và `userLng` vào các API Search hoặc Recommend để BE tính khoảng cách (`distance`).
    3.  **Lưu vết vị trí (Tracking):**
        *   **Chủ động:** Khi người dùng tìm kiếm, vị trí tự động được lưu.
        *   **Thụ động (Background):** Cài đặt một bộ hẹn giờ (ví dụ 15 phút/lần) gọi API `POST /api/locations/track` để BE cập nhật "Vùng hoạt động" của người dùng.
    4.  **Cấu hình quyền (Toggle):** Cung cấp nút gạt "Cho phép lưu lịch trình di chuyển" trong trang cá nhân. Nếu người dùng tắt, hãy gửi `isLocationTrackingEnabled: false` qua API Update User.

---

## 2. Tìm kiếm thông minh (Smart Search)
Biến việc tìm kiếm thành một trải nghiệm "xung quanh tôi".

*   **Nhiệm vụ của FE:**
    1.  **API sử dụng:** `GET /api/places/search`.
    2.  **Tham số quan trọng:** `userLat`, `userLng`, `radius` (bán kính - nên mặc định 2km-5km).
    3.  **Cập nhật UX:** 
        *   Sử dụng cơ chế **"Pull to Refresh"** (vuốt để tải lại) để cập nhật khoảng cách khi người dùng di chuyển.
        *   Hiển thị nhãn `isRecommended: true` (Gợi ý cho bạn) nổi bật hơn trên danh sách kết quả.

---

## 3. Trang chủ & Gợi ý (Personalized Home)
Hiển thị những gì người dùng "thực sự thích".

*   **Nhiệm vụ của FE:**
    1.  **API sử dụng:** `GET /api/recommendations` (API này đã có Redis Cache nên hãy gọi ngay khi mở App).
    2.  **Hiển thị:**
        *   Ưu tiên các địa điểm có `hasActiveEvent: true` lên đầu (vì đây là các điểm đang có lễ hội/sự kiện).
        *   Sử dụng trường `isRecommended` để hiển thị các huy hiệu như "Phù hợp với gu của bạn".

---

## 4. Chat AI & Lịch trình (AI Chat & Itinerary)
Tính năng "Local Buddy" - Biến cuộc hội thoại thành hành động.

*   **Nhiệm vụ của FE:**
    1.  **Giao diện Chat:** Xây dựng màn hình Chat đơn giản. Gửi câu hỏi của User lên API `POST /api/chat`.
    2.  **Xử lý phản hồi (Quan trọng):** API sẽ trả về JSON gồm `introduction`, `timeline` và `suggestedPlaceIds`.
        *   **Timeline:** Vẽ thành một danh sách các bước (Sáng -> Trưa -> Chiều -> Tối). 
        *   **Kết nối dữ liệu:** Sử dụng các `placeId` trong timeline để cho phép người dùng nhấn vào xem chi tiết địa điểm đó.
    3.  **Lưu lịch trình:** Cung cấp nút "Lưu vào kế hoạch của tôi" để người dùng lưu lại lịch trình AI vừa gợi ý.

---

## 5. Phân tích luồng hoạt động (The "Flywheel" Effect)
Để hệ thống càng dùng càng thông minh, FE cần tuân thủ luồng này:

1.  **Giai đoạn 1 (Lấy thông tin):** FE gửi sở thích (Category) khi User đăng ký.
2.  **Giai đoạn 2 (Theo dõi):** FE gửi tọa độ đều đặn. BE sẽ biết User này hay ở Cầu Giấy nhưng cuối tuần hay lên Hoàn Kiếm.
3.  **Giai đoạn 3 (Gợi ý):** Khi User mở mục "Gợi ý", BE sẽ ưu tiên các quán Cafe ở Cầu Giấy (vì đang ở gần) và các sự kiện ở Hoàn Kiếm (vì User hay lên đó chơi).
4.  **Giai đoạn 4 (Tối ưu):** Khi User yêu cầu AI lên lịch trình, AI sẽ bốc các quán trong danh sách gợi ý trên và sắp xếp chúng theo đường đi ngắn nhất (không đi zig-zag giữa các Quận).

---

### 💡 Lời khuyên cho FE:
*   **Sai số GPS:** Hãy xử lý hiển thị khoảng cách làm tròn đến 1 chữ số thập phân (ví dụ 1.2km) để tránh gây rối khi GPS nhảy sai số vài mét.
*   **Hiệu ứng trống (Empty State):** Nếu User chưa bật GPS, hãy luôn yêu cầu họ bật để trải nghiệm tính năng "Tìm quanh đây" tốt nhất.
*   **Sự kiện:** Luôn ưu tiên hiển thị các điểm có Sự kiện trước, vì người dùng du lịch luôn sợ bỏ lỡ (FOMO) các hoạt động đang diễn ra.
