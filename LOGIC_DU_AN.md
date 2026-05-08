#  LOGIC DỰ ÁN & HƯỚNG DẪN TÍCH HỢP FRONTEND - TRIP4HANOI

Tài liệu này tổng hợp toàn bộ logic nghiệp vụ cốt lõi và hướng dẫn kỹ thuật dành cho việc phát triển ứng dụng Trip4Hanoi.

---

## 1. Hệ thống Vị trí & Khoảng cách (Location UX)
Backend đã có sẵn "cỗ máy" tính toán khoảng cách và lưu vết. FE cần đóng vai trò là người cung cấp dữ liệu đầu vào (GPS).

*   **Nhiệm vụ của Frontend:**
    *   **Lấy tọa độ GPS:** Sử dụng `navigator.geolocation` (Web) hoặc thư viện `Geolocator` (Mobile).
    *   **Gửi tọa độ khi Search:** Luôn đính kèm `userLat` và `userLng` vào các API Search/Filter để nhận về kết quả có kèm trường `distance` (km).
    *   **Tracking ngầm (Background Tracking):** Thiết lập một interval (ví dụ 15 phút/lần) để gọi API `POST /api/locations/track`. Việc này giúp Backend xác định được "Vùng hoạt động" (Hot Zone) của User để gợi ý chính xác hơn.
    *   **Cập nhật thủ công:** Hỗ trợ tính năng "Pull to Refresh" trong danh sách địa điểm để lấy GPS mới và cập nhật lại khoảng cách thực tế.

---

## 2. Quản lý Quyền Riêng tư (Privacy Management)
Chúng ta đã triển khai quyền `isLocationTrackingEnabled` trong Database.

*   **Nhiệm vụ của Frontend:**
    *   **Nút Toggle:** Tạo một công tắc (Switch) trong phần "Cài đặt tài khoản" để User bật/tắt quyền theo dõi vị trí.
    *   **Kiểm soát:** Nếu User tắt (`false`), FE nên dừng các lệnh gọi API tracking ngầm để tiết kiệm pin và tôn trọng quyền riêng tư.

---

## 3. Lịch trình Thông minh (Smart Itinerary)
Hệ thống tạo lịch trình không còn là "chọn bừa", mà là sự kết hợp giữa Gợi ý (Recommendation) và Sắp xếp (Scheduling).

*   **Logic Backend đã xử lý:**
    *   **Phễu lọc 2 tầng:** Lọc ra 50 điểm tốt nhất (Gu, Hot Zone, Rating) -> Xếp vào các buổi (Sáng, Trưa, Chiều, Tối).
    *   **Gom cụm Quận (District Clustering):** Hạn chế di chuyển zig-zag. User sẽ chơi hết các điểm ở Hoàn Kiếm rồi mới di chuyển sang Cầu Giấy.
    *   **Ưu tiên Sự kiện:** Nếu có Lễ hội/Sự kiện, lịch trình sẽ cố gắng ưu tiên đưa điểm đó vào.

*   **Nhiệm vụ của Frontend:**
    *   **Hiển thị Timeline:** Thiết kế giao diện theo dạng dòng thời gian (Timeline) với các biểu tượng tương ứng (Sáng: ☀️, Trưa: 🌤️, Tối: 🌙).
    *   **Hiển thị khoảng cách giữa các chặng:** Backend trả về danh sách có thứ tự, FE có thể hiển thị khoảng cách từ điểm A sang điểm B để User chuẩn bị phương tiện di chuyển.

---

## 4. Chat AI Cá nhân hóa (AI Chatbot)
AI không chỉ trả lời văn bản mà trả về Dữ liệu cấu trúc (JSON).

*   **Nhiệm vụ của Frontend:**
    *   **Parse JSON:** AI trả về object có `introduction`, `timeline`, `summary` và `suggestedPlaceIds`. FE cần bóc tách để hiển thị thành các Card địa điểm đẹp mắt thay vì chỉ hiện chữ thô.
    *   **Deep Link:** Sử dụng danh sách `suggestedPlaceIds` để khi User nhấn vào một địa điểm trong Chat, FE sẽ chuyển hướng ngay đến trang chi tiết của địa điểm đó.

---

## 5. Danh sách Gợi ý (Personalized Feed)
Kết quả từ `GET /api/recommendations` đã được Redis Cache bảo vệ.

*   **Nhiệm vụ của Frontend:**
    *   **Hiển thị Badge "Gợi ý cho bạn":** Dựa vào trường `isRecommended: true` Backend trả về để gắn thêm nhãn "Hợp gu bạn" hoặc "Dành riêng cho bạn" trên giao diện.
    *   **Hiển thị Badge "Đang có sự kiện":** Dựa vào trường `hasActiveEvent: true` để gắn biểu tượng 🔥 hoặc 🎁 lên card địa điểm.

---

## 📅 KẾ HOẠCH TRIỂN KHAI CHO TEAM FE

| Tính năng | Độ ưu tiên | Lưu ý kỹ thuật |
| :--- | :--- | :--- |
| **Lấy GPS & Search** | ⭐⭐⭐⭐⭐ | Cần xử lý trường hợp User từ chối cấp quyền GPS (dùng mặc định Hà Nội). |
| **Giao diện Timeline** | ⭐⭐⭐⭐ | Hiển thị lịch trình đa ngày (Day 1, Day 2, Day 3). |
| **Bật/Tắt Tracking** | ⭐⭐⭐ | Gọi API `PUT /api/users/{id}` để cập nhật. |
| **Giao diện Chat AI** | ⭐⭐⭐⭐ | Xử lý trạng thái Loading khi chờ AI phản hồi. |

---

## 💡 Luồng hoạt động tổng thể (Flywheel Effect)

1.  **FE gửi sở thích** khi User đăng ký.
2.  **FE gửi tọa độ** đều đặn để BE xác định "Vùng hoạt động".
3.  **BE gợi ý quán xịn** dựa trên Gu + Vị trí + Sự kiện.
4.  **AI sắp xếp lịch trình** tối ưu quãng đường dựa trên các quán xịn đó.
5.  **User hài lòng** và tiếp tục sử dụng App.
