Dưới đây là cơ chế hoạt động của tính năng tìm kiếm theo khoảng cách:

1. Phía Frontend (Người dùng)
* Lấy tọa độ: Khi người dùng mở ứng dụng và nhấn "Tìm quanh đây", ứng dụng (trên điện thoại hoặc trình duyệt) sẽ sử dụng Geolocation API (có sẵn trên Android/iOS/Web) để xin quyền truy cập vị trí.
* Kết quả: Thiết bị sẽ trả về một cặp tọa độ chính xác: Latitude (Vĩ độ) và Longitude (Kinh độ).
    * Ví dụ: Nhà Hát Lớn Hà Nội có tọa độ khoảng (21.0242, 105.8588).
* Gửi API: Frontend sẽ đính kèm cặp tọa độ này vào request gửi lên Backend của bạn.

2. Phía Backend (Xử lý - Phần của bạn)
   Khi nhận được tọa độ từ người dùng, Backend sẽ thực hiện một trong hai cách sau để lọc:

Cách A: Tính toán bằng công thức toán học (Haversine Formula)
Backend sẽ chạy một câu truy vấn SQL để tính khoảng cách giữa tọa độ người dùng gửi lên và tọa độ của tất cả các Place trong Database.
* Công thức: Haversine giúp tính khoảng cách giữa 2 điểm trên mặt cầu (Trái Đất).
* Logic: Tìm tất cả Place mà KhoảngCách(TọaĐộNgườiDùng, TọaĐộĐịaĐiểm) <= BánKính (ví dụ 3km).

Cách B: Sử dụng tính năng Spatial của Database (Chuyên nghiệp hơn)
Nếu bạn dùng PostgreSQL (PostGIS) hoặc MySQL/SQL Server đời mới:
* Chúng có các hàm như ST_Distance_Sphere.
* Database sẽ xử lý việc tính toán này cực nhanh nhờ vào "Spatial Index" (Chỉ mục không gian).

  ---

3. Thiết kế API Endpoint (Plan)

Dưới đây là bản thiết kế Request và Response cho một API "Siêu lọc" kết hợp tất cả các yếu tố chúng ta đã bàn:

Endpoint: GET /api/places/search hoặc POST /api/places/filter (Dùng POST nếu bạn muốn gửi nhiều tiêu chí phức tạp).

Request DTO (Dữ liệu khách hàng gửi lên):

     {                                                                                                                                                                                                                                                                                                             
       "keyword": "Cà phê trứng",                                                                                                                                                                                                                                                                                  
       "categoryId": 2,                                                                                                                                                                                                                                                                                            
       "district": "Hoàn Kiếm",                                                                                                                                                                                                                                                                                    
       "minPrice": 30000,                                                                                                                                                                                                                                                                                          
       "maxPrice": 100000,                                                                                                                                                                                                                                                                                         
       "minRating": 4.0,                                                                                                                                                                                                                                                                                           
                                                                                                                                                                                                                                                                                                                   
       // Thông tin để tính khoảng cách                                                                                                                                                                                                                                                                            
   "userLat": 21.0242,                                                                                                                                                                                                                                                                                         
   "userLng": 105.8588,                                                                                                                                                                                                                                                                                        
   "radius": 3.0, // Tìm trong bán kính 3km                                                                                                                                                                                                                                                                    
                                                                                                                                                                                                                                                                                                               
   "sortBy": "distance", // Sắp xếp theo: distance, rating, price, hoặc popular                                                                                                                                                                                                                                
   "page": 0,                                                                                                                                                                                                                                                                                                  
   "size": 10                                                                                                                                                                                                                                                                                                  
 }

Response DTO (Dữ liệu trả về cho người dùng):
Mỗi địa điểm trả về sẽ được Backend tính toán thêm một trường dữ liệu ảo là distance.

     {                                                                                                                                                                                                                                                                                                             
       "status": 200,                                                                                                                                                                                                                                                                                              
       "data": [                                                                                                                                                                                                                                                                                                   
         {                                                                                                                                                                                                                                                                                                         
           "id": 101,                                                                                                                                                                                                                                                                                              
           "name": "Cafe Giảng",                                                                                                                                                                                                                                                                                   
           "address": "74 Nguyễn Hữu Huân",                                                                                                                                                                                                                                                                        
           "distance": "0.5 km", // Backend tính toán và trả về                                                                                                                                                                                                                                                    
           "ratingAvg": 4.8,                                                                                                                                                                                                                                                                                       
       "priceAvg": 45000,                                                                                                                                                                                                                                                                                      
       "imageUrl": "...",                                                                                                                                                                                                                                                                                      
       "hasActiveEvent": true // Đang có sự kiện diễn ra                                                                                                                                                                                                                                                       
     },                                                                                                                                                                                                                                                                                                        
     ...                                                                                                                                                                                                                                                                                                       
   ],                                                                                                                                                                                                                                                                                                          
   "totalElements": 45,                                                                                                                                                                                                                                                                                        
   "totalPages": 5                                                                                                                                                                                                                                                                                             
 }

4. Tại sao cách này lại "sống"?
+Tính thời điểm: Nếu người dùng di chuyển từ Nhà Hát Lớn sang Hồ Tây và nhấn Refresh, danh sách địa điểm sẽ thay đổi hoàn toàn dựa trên tọa độ mới.
+Tính thực tế: Thay vì hiện ra hàng trăm quán Cafe ở khắp Hà Nội, ứng dụng chỉ hiện 10 quán tốt nhất trong vòng 15 phút đi bộ của họ.

Kế hoạch tiếp theo: Bạn có muốn tôi hướng dẫn cách thiết kế logic trong Service để xử lý cái "Search Engine" tổng hợp này không (vẫn là lên plan logic, chưa cần viết code chi tiết)?                                                                                                                             

======================================================================================================================================================================================================================



Chiến lược xử lý tại lớp Service

Chúng ta sẽ chia logic xử lý ra làm 5 giai đoạn chính:

Giai đoạn 1: Thu thập và Tiền xử lý (Preparation)
* Nhận DTO: Service nhận PlaceFilterRequest từ Controller.
* Xác định ngữ cảnh: Kiểm tra xem người dùng có đang đăng nhập không (để lấy UserPreference) và có gửi tọa độ (lat, lng) lên không.
* Chuẩn hóa: Chuyển đổi từ khóa tìm kiếm về dạng không dấu hoặc viết thường để tìm kiếm chính xác hơn.

Giai đoạn 2: Xây dựng bộ lọc động (Dynamic Filtering)
Sử dụng JPA Specification để xây dựng câu lệnh WHERE linh hoạt:
* IF categoryId != null -> Thêm điều kiện category_id = :id.
* IF district != null -> Thêm điều kiện district LIKE :district.
* IF minPrice/maxPrice != null -> Thêm điều kiện price_avg BETWEEN :min AND :max.
* IF keyword != null -> Thêm điều kiện (name LIKE :key OR description LIKE :key).

Giai đoạn 3: Xử lý Khoảng cách (Proximity Logic)
Đây là phần "khó" nhất. Service sẽ cần thực hiện:
* Tính toán tại Database: Thay vì lấy hết dữ liệu về rồi mới tính (rất chậm), chúng ta sẽ truyền công thức Haversine trực tiếp vào câu lệnh SQL.
* Công thức tóm tắt: ACOS(SIN(lat1)*SIN(lat2) + COS(lat1)*COS(lat2)*COS(lng2-lng1)) * 6371.
* Lọc bán kính: Database sẽ chỉ trả về những Place có kết quả công thức trên <= radius (ví dụ 3km).

Giai đoạn 4: Kiểm tra sự kiện (Event Check)
* Service sẽ thực hiện một lệnh LEFT JOIN với bảng Event.
* Điều kiện: Một địa điểm được coi là "Có sự kiện" nếu tồn tại ít nhất một Event có status = ACTIVE và thời gian hiện tại nằm trong khoảng startTime và endTime.

Giai đoạn 5: Sắp xếp và Cá nhân hóa (Ranking & Personalization)
Sau khi có danh sách đã lọc, Service sẽ quyết định thứ tự hiển thị:
1. Ưu tiên 1 (Khoảng cách): Nếu người dùng chọn "Tìm quanh đây", các điểm gần nhất sẽ lên đầu.
2. Ưu tiên 2 (Sở thích): Nếu người dùng thích "Cafe" (trong UserPreference), Service sẽ cộng thêm một "điểm ưu tiên" (score boost) để các quán Cafe nổi lên trên các kết quả khác dù có thể nó xa hơn một chút.
3. Ưu tiên 3 (Đánh giá): Những nơi có ratingAvg cao và nhiều viewCount sẽ được ưu tiên nếu các yếu tố khác bằng nhau.

  ---

Quy trình dữ liệu chảy trong Service (Workflow)

1. Bắt đầu: Nhận yêu cầu tìm kiếm.
2. Bước 1: Xây dựng câu lệnh SQL động dựa trên các bộ lọc cơ bản (Category, Price, District).
3. Bước 2: Nếu có tọa độ, tích hợp công thức Haversine vào SQL để lọc theo bán kính và tính khoảng cách.
4. Bước 3: Gửi câu lệnh xuống Database thông qua Repository.
5. Bước 4: Nhận danh sách kết quả (đã phân trang).
6. Bước 5: Với mỗi kết quả, kiểm tra xem nó có trùng với UserPreference của người dùng không để đánh dấu "Gợi ý cho bạn".
7. Kết thúc: Map dữ liệu sang PlaceResponse (kèm theo thông tin khoảng cách đã tính) và trả về.

  ---

Lợi ích của Plan này:
* Hiệu năng cao: Tính toán khoảng cách và lọc dữ liệu diễn ra ở Database, Backend chỉ nhận về đúng số lượng bản ghi cần thiết (ví dụ 10 bản ghi cho trang 1).
* Mở rộng tốt: Sau này bạn muốn thêm lọc theo "Giờ mở cửa" hay "Tiện ích (Wifi, Chỗ đỗ xe)", bạn chỉ cần thêm một Predicate vào Specification mà không làm hỏng logic cũ.
* Trải nghiệm người dùng: Kết quả trả về rất sát với nhu cầu thực tế (Gần - Rẻ - Đúng sở thích).                                                                                                                                                                                                                 
