# Lộ trình Phát triển Dự án Trip4Hanoi (Project Roadmap & Implementation Plan)

## 1. Tầm nhìn & Chiến lược (Vision & Strategy)
Trip4Hanoi không chỉ là một ứng dụng du lịch, mà là một **"Người bạn bản địa số"**. 
- **Khẩu hiệu:** "Không phải đi nhiều hơn, mà là đi đúng hơn."
- **USP:** Cá nhân hóa lịch trình dựa trên AI + Dữ liệu văn hóa/lịch sử được kiểm chứng + Cập nhật sự kiện thời gian thực.

---

## 2. Kiến trúc Kỹ thuật Đề xuất (Technical Architecture)
Dựa trên cấu trúc Spring Boot (Java) hiện có:

### Backend Stack:
- **Framework:** Spring Boot 3.x
- **Database:** MySQL (Lưu trữ dữ liệu quan hệ: User, Locations, Itineraries).
- **Caching:** Redis (Lưu trữ lịch trình tạm thời và kết quả gợi ý nhanh).
- **AI Integration:** Spring AI (kết nối với Gemini API hoặc OpenAI) để xử lý logic lập lịch trình tự động.
- **Maps:** Google Maps API (Tính toán khoảng cách, hiển thị bản đồ).
- **Security:** Spring Security + JWT (Xác thực người dùng).

### Frontend (Gợi ý):
- **Giai đoạn 1:** React.js hoặc Next.js (Web App) để tối ưu SEO và tiếp cận nhanh.
- **Giai đoạn 2:** Flutter hoặc React Native (Mobile App) để tăng trải nghiệm thực địa.

---

## 3. Lộ trình Triển khai (Phased Roadmap)

### Giai đoạn 1: Xây dựng Nền tảng & MVP (Tháng 1 - 2)
**Mục tiêu:** Hoàn thành các tính năng cốt lõi để kiểm chứng thị trường.
- **Kỹ thuật:**
    - Thiết kế Database Schema (User, Location, Category, Review).
    - Xây dựng API quản lý địa điểm (CRUD).
    - Tích hợp logic lọc địa điểm cơ bản theo Sở thích/Ngân sách.
- **Nội dung:**
    - Thu thập dữ liệu 50-100 địa điểm "chất" tại Hà Nội (Ăn, Xem, Chơi).
    - Biên tập nội dung văn hóa ngắn gọn cho từng điểm.
- **Kinh doanh:**
    - Chạy Landing Page thu thập email đăng ký sớm.

### Giai đoạn 2: Trí tuệ nhân tạo & Cá nhân hóa (Tháng 3 - 4)
**Mục tiêu:** Tạo ra sự khác biệt với đối thủ bằng AI.
- **Kỹ thuật:**
    - Triển khai thuật toán AI Generator: Nhận input (Thời gian, Sở thích) -> Output (Lịch trình tối ưu đường đi).
    - Tích hợp Google Maps API để hiển thị lộ trình trực quan.
    - Xây dựng tính năng "Lưu và Chia sẻ" lịch trình.
- **Nội dung:**
    - Cập nhật module "Sự kiện thời gian thực" (Real-time events).
- **Kinh doanh:**
    - Hợp tác với 10-20 đối tác F&B/Homestay đầu tiên (Affiliate model).

### Giai đoạn 3: Tối ưu hóa & Thương mại hóa (Tháng 5 - 6)
**Mục tiêu:** Ra mắt bản Pro và bắt đầu tạo doanh thu.
- **Kỹ thuật:**
    - Tích hợp cổng thanh toán (VNPay/Momo) cho các gói Pro/Premium.
    - Xây dựng hệ thống Notification nhắc lịch trình.
    - Hoàn thiện hệ thống Dashboard cho đối tác (B2B).
- **Kinh doanh:**
    - Chiến dịch Marketing TikTok/Reels mạnh (Gen Z focus).
    - Triển khai QR Code tại các điểm chạm offline (Khách sạn, Cafe).

---

## 4. Kế hoạch Hành động Ngay (Immediate Action Plan)

| Việc cần làm | Trách nhiệm | Ưu tiên |
| :--- | :--- | :--- |
| **Thiết kế Database ERD** | Backend Team | Cao |
| **Thu thập Data 50 địa điểm mẫu** | Content Team | Cao |
| **Thiết kế UI/UX cho luồng Tạo lịch trình** | Design Team | Cao |
| **Cấu hình Spring Security & JWT** | Backend Team | Trung bình |
| **Tìm kiếm 5 đối tác đầu tiên để thử nghiệm** | Business Team | Trung bình |

---[GeminiServiceImpl.java](src/main/java/com/trip4hanoi/app/service/impl/GeminiServiceImpl.java)

## 5. Quản trị Rủi ro (Risk Management)
1. **Dữ liệu lỗi thời:** Thiết lập quy trình kiểm tra dữ liệu hàng tuần (Manual + Crowdsourced).
2. **Chi phí API cao:** Tối ưu hóa cache kết quả Maps và AI, giới hạn số lần query cho tài khoản Free.
3. **Cạnh tranh từ mạng xã hội:** Tập trung vào tính "tiện lợi" và "lịch trình sẵn có", điều mà TikTok/Facebook không làm được một cách hệ thống.

---
*Bản kế hoạch này được tổng hợp dựa trên dữ liệu BMC và phân tích kỹ thuật hiện tại của dự án Trip4Hanoi.*
