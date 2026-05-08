#  TÀI LIỆU NGHIỆP VỤ & LOGIC HỆ THỐNG TRIP4HANOI

Tài liệu này chi tiết về logic nghiệp vụ và kỹ thuật cho các thành phần cốt lõi của hệ thống **Trip4Hanoi**.

---

## 1. Địa điểm (Place)
*   **Logic nghiệp vụ:** Là thực thể trung tâm lưu trữ "hồn" của các điểm đến. Hệ thống quản lý không chỉ thông tin cơ bản mà cả dữ liệu không gian.
*   **Công dụng:** Cung cấp tọa độ, mô tả, danh mục và hình ảnh cho toàn bộ ứng dụng.
*   **Đã làm được:**
    *   **Tính toán khoảng cách:** Tích hợp công thức **Haversine** để tính khoảng cách chính xác theo đường chim bay từ vị trí User đến địa điểm.
    *   **Tối ưu truy vấn:** Sử dụng `@EntityGraph` để tải dữ liệu (Category, Images) trong 1 lần truy vấn duy nhất, triệt tiêu lỗi LazyInitialization khi chạy Async.
*   **Cách test/sử dụng:** 
    *   Gọi API `GET /api/places?userLat=...&userLng=...` để thấy danh sách kèm khoảng cách.
    *   Test Cache: Khi Admin sửa thông tin một điểm, kết quả gợi ý phải được cập nhật ngay lập tức nhờ logic `@CacheEvict`.
*   **Mối liên hệ:** Là "gốc" để gắn **Sự kiện**, là "đầu vào" cho **Gợi ý** và **AI Chat**.

## 2. Sự kiện (Event)
*   **Logic nghiệp vụ:** Các hoạt động diễn ra trong thời gian nhất định (Lễ hội, khuyến mãi, workshop).
*   **Công dụng:** Tạo sự biến động và hấp dẫn cho các địa điểm cố định.
*   **Đã làm được:** 
    *   **Nhận diện sự kiện sống:** Logic lọc tự động `startTime <= Now <= endTime`.
    *   **Cú hích gợi ý:** Một địa điểm có sự kiện đang diễn ra sẽ nhận được điểm thưởng cao nhất để xuất hiện ở top đầu.
*   **Cách test/sử dụng:** Tạo một sự kiện tại quán Phở, sau đó Chat với AI. AI sẽ phải nhắc đến sự kiện này đầu tiên trong lịch trình.
*   **Mối liên hệ:** Thuộc về một **Place**. Tác động trực tiếp đến trọng số của **Recommendation**.

## 3. Bộ máy Gợi ý (Recommendation)
*   **Logic nghiệp vụ (Scoring Logic):** Hệ thống tính điểm đa tiêu chí:
    *   **Sở thích (+1.5đ):** Khớp với danh mục User đã lưu.
    *   **Vùng hoạt động (+2.0đ):** Ưu tiên các Quận mà User thường xuyên ghé thăm (Dựa trên lịch sử vị trí 15 ngày).
    *   **Sự kiện (+5.0đ):** Điểm thưởng lớn nhất cho nơi có hoạt động đặc biệt.
*   **Công dụng:** Lọc ra 15 địa điểm "tinh túy" nhất từ hàng ngàn điểm trong DB.
*   **Đã làm được:** 
    *   **Redis Caching:** Lưu kết quả trong 30 phút. Tốc độ phản hồi từ ~500ms giảm xuống **< 50ms**.
    *   **N+1 Optimization:** Lấy toàn bộ sự kiện vào Set để đối soát, tránh truy vấn Database lặp đi lặp lại.
*   **Mối liên hệ:** Kết nối **User**, **Place**, **Event** và **Location History** thành một danh sách ưu tiên.

## 4. Lịch trình (Itinerary)
*   **Logic nghiệp vụ:** Cấu trúc theo dạng Ngày -> Buổi -> Hoạt động.
*   **Công dụng:** Giúp người dùng hình dung hành trình thực tế.
*   **Đã làm được:** 
    *   Tích hợp sâu với AI để tự động tạo Timeline từ yêu cầu bằng ngôn ngữ tự nhiên.
    *   Hỗ trợ lưu trữ ID địa điểm thực tế để Frontend có thể hiển thị ảnh và bản đồ di chuyển.
*   **Mối liên hệ:** Là "sản phẩm cuối cùng" mà **AI Chat** trả về hoặc **User** tự tay lên kế hoạch từ danh sách **Place**.

## 5. Chat AI (Gemini Flash)
*   **Logic nghiệp vụ:** Đóng vai một "Người bạn bản địa Hà Nội" (Local Buddy).
*   **Công dụng:** Giải đáp mọi thắc mắc và tự động thiết kế lịch trình.
*   **Đã làm được:** 
    *   **Parallel Context Fetching:** Lấy dữ liệu cá nhân hóa và địa điểm cùng lúc, giảm thời gian chuẩn bị dữ liệu xuống còn gần như 0.
    *   **Prompt Compression:** Nén dữ liệu địa điểm thành dạng ký hiệu `[ID]Tên(Quận).Gu:true` giúp AI đọc nhanh và chính xác hơn.
    *   **Strict JSON Protocol:** Ép AI trả về JSON chuẩn, không có markdown để Frontend hiển thị mượt mà.
*   **Cách test/sử dụng:** Thử các prompt có ngân sách (Budget), thời tiết hoặc số lượng người đi. Ví dụ: *"Đi chơi 3 người, ngân sách 1tr, thích chill ở Cầu Giấy"*.
*   **Mối liên hệ:** Là "Nhạc trưởng" điều phối **Recommendation engine** để lấy dữ liệu và trả về cho **User**.

---

### Mối liên hệ tổng thể:
1.  **User** Chat với **AI**.
2.  **AI** gọi **Recommendation** (đã được **Redis Cache** bảo vệ tốc độ).
3.  **Recommendation** quét **Place** & **Event** để tính điểm theo **User Gu** & **Location**.
4.  Kết quả trả về cho **User** dưới dạng một **Itinerary** trực quan.
