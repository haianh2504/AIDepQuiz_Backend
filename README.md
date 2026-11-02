# AIDepQuiz – Backend

Backend REST API cho AIDepQuiz, ứng dụng quiz giúp người dùng tự đánh giá mức độ phụ thuộc vào AI trong học tập và công việc. Backend chịu trách nhiệm cung cấp nội dung quiz, kiểm tra đáp án được gửi lên, tính tổng điểm và trả về mức đánh giá cùng lời khuyên được chuẩn bị trước.

> Tài liệu này được xây dựng dựa trên README frontend và trạng thái hiện tại của repository backend. Repository hiện mới là Spring Boot skeleton; các API, entity, schema và rate limiter chưa được triển khai.

## 1. Trạng thái và phạm vi phiên bản đầu tiên

### Trong phạm vi

- REST API cho frontend Next.js.
- Quiz gồm đúng **10 câu hỏi**.
- Câu hỏi được lưu cố định trong PostgreSQL và trả về theo thứ tự đã lưu; backend không random câu hỏi trong mỗi lượt làm bài.
- Mỗi câu hỏi có các lựa chọn và điểm số tương ứng được quản lý ở backend.
- Nhận request chứa `questionId` và `optionId` cho đủ 10 câu hỏi.
- Tính tổng điểm ở backend và trả về mức độ phụ thuộc vào AI, nhận xét và lời khuyên được chuẩn bị trước.
- Không yêu cầu đăng nhập và không lưu lịch sử kết quả cá nhân trong phiên bản hiện tại.
- Áp dụng rate limiter cho các REST endpoint sau khi cơ chế giới hạn được chốt và triển khai.

### Ngoài phạm vi hiện tại

- Đăng ký, đăng nhập, session hoặc phân quyền người dùng.
- Admin dashboard hoặc API quản trị nội dung.
- Cá nhân hóa lời khuyên bằng AI theo thời gian thực.
- Lưu kết quả để tính top phần trăm hoặc tổng số người đã hoàn thành quiz.
- API contract chính thức, migration tool và quy trình import Excel hoàn chỉnh — các mục này cần được định nghĩa trước khi triển khai production.

## 2. Công nghệ

| Thành phần | Công nghệ / trạng thái |
|---|---|
| Ngôn ngữ | Java 21 |
| Framework | Spring Boot 4.1.2 (được khai báo trong `pom.xml`) |
| Web/API | Spring MVC / Spring WebMVC |
| Persistence | Spring Data JPA và Hibernate |
| Database | PostgreSQL |
| Database hosting | Supabase |
| Backend hosting | Render |
| Build tool | Maven Wrapper (`./mvnw`, `mvnw.cmd`) |
| Authentication | Không có trong phiên bản hiện tại |
| API style | REST |

Các dependency hiện tại còn gồm JDBC, PostgreSQL driver, Lombok, DevTools và Docker Compose support. Dependency và cấu hình có thể thay đổi khi các use case được triển khai.

## 3. Quick start

### Yêu cầu

- JDK 21.
- Maven không bắt buộc nếu sử dụng Maven Wrapper.
- PostgreSQL khi chạy integration test hoặc ứng dụng có truy cập database.
- Docker Desktop nếu sử dụng `compose.yaml`.

### Cài đặt và chạy

Clone repository, chuyển vào thư mục backend và chạy:

```bash
./mvnw test
./mvnw spring-boot:run
```

Trên Windows:

```bat
mvnw.cmd test
mvnw.cmd spring-boot:run
```

Địa chỉ HTTP mặc định và danh sách endpoint sẽ được bổ sung sau khi controller và API contract được định nghĩa. Hiện `application.properties` mới khai báo tên ứng dụng và chưa có cấu hình datasource hoàn chỉnh.

## 4. API dự kiến

API contract chính thức chưa tồn tại. Backend cần triển khai tối thiểu hai use case tương ứng với frontend:

| Mã | Method | Mục đích |
|---|---|---|
| UC01 | `GET` | Trả về 10 câu hỏi cùng các lựa chọn theo thứ tự cố định. |
| UC02 | `POST` | Nhận đáp án của đủ 10 câu, tính điểm và trả về kết quả quiz. |

Các điểm phải được chốt trong `openapi.yaml` hoặc tài liệu contract trước khi frontend tích hợp:

- URL chính xác của từng endpoint.
- Schema request/response và tên field.
- Cấu trúc danh sách `questionId`/`optionId`.
- Quy tắc khi thiếu câu trả lời, thiếu câu hỏi hoặc option không thuộc câu hỏi.
- HTTP status và error body cho lỗi validation, dữ liệu không tồn tại và lỗi hệ thống.
- Cách xử lý request submit lặp lại khi hệ thống không lưu lịch sử kết quả.
- Rate-limit policy và response khi vượt giới hạn, dự kiến sử dụng HTTP `429`.

Frontend không nên tự suy đoán endpoint hoặc response shape khi contract chưa được thống nhất.

## 5. Luồng dữ liệu

```text
Next.js frontend
        ↓ REST request
Spring MVC Controller
        ↓
Application / Service layer
        ↓
Spring Data JPA + Hibernate
        ↓
PostgreSQL trên Supabase
```

Điểm số phải được tính ở backend dựa trên dữ liệu trong database; frontend chỉ gửi lựa chọn của người dùng và hiển thị response kết quả.

## 6. Cấu trúc repository hiện tại

```text
Backend/
├── pom.xml
├── mvnw
├── mvnw.cmd
├── compose.yaml
├── src/
│   ├── main/
│   │   ├── java/org/example/backend/BackendApplication.java
│   │   └── resources/application.properties
│   └── test/java/org/example/backend/BackendApplicationTests.java
└── README_backend.md
```

Đây là **cấu trúc hiện tại đã kiểm tra trong repository**, chưa phải cấu trúc hoàn chỉnh của feature quiz. Khi triển khai, có thể bổ sung các package sau:

```text
org.example.backend/
├── controller/
├── service/
├── repository/
├── entity/
├── dto/
├── exception/
└── config/
```

Các package trên là **cấu trúc dự kiến**, không được xem là file đã tồn tại.

## 7. Database và dữ liệu nội dung

PostgreSQL là database chính và sẽ được host trên Supabase. Dữ liệu cần được quản lý gồm:

- Câu hỏi và thứ tự hiển thị.
- Các lựa chọn thuộc từng câu hỏi.
- Điểm của từng lựa chọn.
- Mức đánh giá theo khoảng điểm.
- Nhận xét và lời khuyên tương ứng.

README frontend định hướng chuẩn bị nội dung bằng file Excel rồi import vào PostgreSQL thông qua backend. Schema, migration framework, format Excel, seed data và lệnh import **chưa được định nghĩa trong repository hiện tại**; cần bổ sung trước khi hướng dẫn vận hành database.

`compose.yaml` hiện có service PostgreSQL mẫu với database/user/password mẫu và port container `5432`. Đây chỉ là cấu hình phát triển ban đầu, không dùng thông tin mẫu này cho môi trường production. Cấu hình datasource local và Supabase chưa được hoàn thiện.

## 8. Cấu hình môi trường

Hiện chưa có `.env.example` và chưa có danh sách biến môi trường được chuẩn hóa. Khi cấu hình datasource, cần bổ sung tài liệu cho các nhóm giá trị sau mà không commit secret:

- JDBC URL PostgreSQL.
- Username và password database.
- Hibernate/JPA profile phù hợp với local và production.
- Port ứng dụng nếu khác mặc định.
- Rate-limit configuration.
- CORS origin của frontend Vercel và local.

Secret của Supabase, Render và database phải được lưu trong secret/environment settings của môi trường chạy, không đặt trong source code hoặc commit vào repository.

## 9. Rate limiter

Rate limiter là yêu cầu bắt buộc của backend nhưng **chưa được triển khai trong code hiện tại**. Thiết kế cần chốt trước khi implement:

- Giới hạn theo IP, theo route, hoặc kết hợp cả hai.
- Cửa sổ thời gian và số request tối đa.
- Các endpoint áp dụng, tối thiểu là endpoint lấy quiz và submit quiz.
- Cách xử lý proxy của Render để lấy client IP an toàn.
- Response `429 Too Many Requests`, header liên quan và thông tin retry nếu có.
- Storage của counter trong môi trường nhiều instance; không nên chỉ dùng memory local nếu Render scale nhiều instance.

Rate limiter không thay thế validation, giới hạn payload và các biện pháp bảo vệ database.

## 10. Kiểm thử và kiểm tra chất lượng

Test hiện tại là Spring Boot context test trong `BackendApplicationTests`. Chạy bằng:

```bash
./mvnw test
```

Khi API được triển khai, cần bổ sung test cho:

- Lấy đúng 10 câu hỏi theo thứ tự cố định.
- Submit đủ 10 câu và tính điểm đúng.
- Thiếu câu trả lời, duplicate `questionId`, option sai và option không thuộc câu hỏi.
- Response error và HTTP status.
- Không yêu cầu authentication.
- Rate limiter và HTTP `429`.
- Repository/service với PostgreSQL test phù hợp.
- CORS và integration với frontend.

Lệnh kiểm tra build Maven:

```bash
./mvnw package
```

Các test integration, migration và contract test chưa được cấu hình trong repository hiện tại.

## 11. Deployment

Render là host backend dự kiến; Supabase là host PostgreSQL dự kiến. Chi tiết deploy chưa được cấu hình trong repository, vì vậy các giá trị sau vẫn cần xác định trên Render:

- Build command và start command chính thức.
- Java runtime/build image.
- Environment variables production.
- Health check endpoint.
- CORS origin của frontend production.
- Migration/seed strategy khi deploy.
- Log, monitoring và rollback procedure.

Không đưa credential Supabase vào README. Sau khi có contract và cấu hình deploy, cập nhật phần này bằng các lệnh thực tế đã kiểm tra trên Render.

## 12. Troubleshooting

### Ứng dụng không khởi động do database

Kiểm tra PostgreSQL đang chạy, datasource configuration đã được cung cấp và thông tin kết nối không dùng giá trị mẫu trong `compose.yaml`.

### Test context thất bại

Chạy lại:

```bash
./mvnw test
```

Sau đó kiểm tra log Spring Boot và cấu hình datasource. Repository hiện chưa có test database độc lập nên môi trường test database cần được bổ sung khi persistence được triển khai.

### Frontend không gọi được backend

Kiểm tra API contract, backend URL, CORS origin và response error thực tế. Không thay đổi request body ở frontend theo phỏng đoán khi contract chưa được cập nhật.

## 13. Đóng góp

Trước khi mở pull request:

1. Cập nhật API contract nếu thay đổi endpoint hoặc schema.
2. Bổ sung test cho use case và error case mới.
3. Chạy `./mvnw test` và `./mvnw package`.
4. Cập nhật README này nếu thay đổi cách chạy, cấu hình, database hoặc deployment.
5. Không commit secret, credential hoặc dữ liệu production.

## 14. Các tài liệu cần bổ sung

- `openapi.yaml` cho REST API.
- Database schema và migration.
- `.env.example` với tên biến đã chuẩn hóa.
- Quy trình import dữ liệu quiz từ Excel.
- Rate-limit policy và implementation.
- Render deployment configuration.
