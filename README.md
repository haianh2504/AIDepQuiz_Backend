# AIDepQuiz – Backend

Backend REST API cho AIDepQuiz, ứng dụng giúp người dùng tự đánh giá mức độ phụ thuộc vào AI trong học tập và công việc. Backend chịu trách nhiệm cung cấp nội dung quiz, kiểm tra câu trả lời, tính điểm, phân loại và trả về nhận xét cùng lời khuyên.

> Tài liệu này được xây dựng dựa trên README dự án và `api-contract.yaml` của Phase 1. Repository hiện mới là Spring Boot skeleton; controller, entity, schema và rate limiter vẫn cần được triển khai.

## 1. Trạng thái và phạm vi phiên bản đầu tiên

### Trong phạm vi

- REST API cho frontend Next.js.
- Quiz gồm đúng **16 câu hỏi**.
- Câu hỏi được chia thành 4 nhóm, mỗi nhóm 4 câu; backend trả về theo thứ tự cố định và không random trong Phase 1.
- Mỗi câu hỏi có 5 lựa chọn Likert với giá trị từ `1` đến `5`.
- Nhận request chứa `questionId` và `value` cho đủ 16 câu hỏi.
- Tính `totalScore`, `mean` và `displayMean` ở backend.
- Trả về category, điểm theo 4 nhóm, nhận xét chung, danh sách lời khuyên và giới hạn diễn giải.
- Không yêu cầu đăng nhập và không lưu lịch sử kết quả cá nhân trong phiên bản hiện tại.
- Áp dụng rate limiter cho các REST endpoint sau khi cơ chế giới hạn được chốt và triển khai.

### Ngoài phạm vi hiện tại

- Đăng ký, đăng nhập, session hoặc phân quyền người dùng.
- Admin dashboard hoặc API quản trị nội dung.
- Cá nhân hóa lời khuyên bằng AI theo thời gian thực.
- Lưu kết quả để tính top phần trăm hoặc tổng số người đã hoàn thành quiz.
- Lưu attempt hoặc lịch sử kết quả người dùng.
- API quản trị nội dung, đăng nhập và phân quyền.
- Migration tool và quy trình import Excel hoàn chỉnh — các mục này cần được định nghĩa trước khi triển khai production.

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

API sử dụng base path `/api/v1`. Danh sách endpoint và schema chính thức nằm trong `api-contract.yaml`. Hiện `application.properties` mới khai báo tên ứng dụng và chưa có cấu hình datasource hoàn chỉnh.

## 4. Use cases và API chính thức

API được mô tả trong file `api-contract.yaml`, sử dụng OpenAPI 3.0.3 và base path:

```text
/api/v1
```

Backend không yêu cầu đăng nhập, không nhận `attemptId` và không lưu lịch sử kết quả cá nhân trong Phase 1.

### Use case

| Mã | Method | Endpoint | Mục đích |
|---|---|---|---|
| UC01 | `GET` | `/api/v1/tests/ai-dependence` | Trả về phiên bản bài test tiếng Việt đang hoạt động, gồm 16 câu hỏi, 5 lựa chọn Likert/câu và quy tắc chấm điểm. |
| UC02 | `POST` | `/api/v1/tests/ai-dependence/submit` | Nhận đủ 16 câu trả lời, kiểm tra dữ liệu, tính điểm ở backend và trả về kết quả hoàn chỉnh. |

### Luồng frontend–backend

```text
/quiz
  ├─ GET  /api/v1/tests/ai-dependence
  ├─ POST /api/v1/tests/ai-dependence/submit
  └─ chuyển sang /result_quiz bằng response kết quả
```

`/result_quiz` là route frontend, không phải API endpoint. Trang này chỉ hiển thị dữ liệu nhận được từ response submit.

### UC01 — Lấy nội dung bài test

Response phải có:

- `testSlug = ai-dependence`.
- `testVersion`, `locale = vi-VN` và tiêu đề bài test.
- Đúng 16 câu hỏi, theo thứ tự cố định.
- Mỗi câu thuộc một trong bốn nhóm: `emotional`, `functional`, `cognitive`, `control`.
- Mỗi câu có đúng 5 lựa chọn với giá trị từ `1` đến `5`.
- `scoring`, gồm khoảng điểm, số chữ số hiển thị, trạng thái item đảo chiều và category ranges.

### UC02 — Submit và chấm điểm

Request chỉ gửi:

```json
{
  "testVersion": "vi-dependence-v1",
  "answers": [
    { "questionId": "dep-01", "value": 4 }
  ]
}
```

Request thực tế phải có đúng 16 câu trả lời. Backend phải kiểm tra:

- Có đúng 16 phần tử.
- Không trùng `questionId`.
- Mỗi `questionId` thuộc đúng phiên bản bài test.
- Mỗi `value` là số nguyên từ `1` đến `5`.
- Không nhận `totalScore`, `mean`, `category`, `feedback` hoặc `advice` từ frontend.

Response thành công gồm:

- `score.totalScore`: tổng điểm từ 16 đến 80.
- `score.mean`: điểm thực dùng để phân loại, từ 1 đến 5.
- `score.displayMean`: điểm đã làm tròn 2 chữ số để hiển thị.
- `category`: key, label và mô tả mức tham khảo.
- `subscales`: điểm của 4 nhóm, sử dụng cùng key cố định.
- `feedback`: chuỗi nhận xét chung.
- `advice`: mảng từ 1 đến 3 chuỗi lời khuyên để hiển thị dạng bullet.
- `limitations`: các giới hạn diễn giải của bài test.

Backend là nguồn duy nhất tính điểm, category, feedback, advice và limitations. Frontend chỉ render response.

### Scoring chính thức

```text
totalScore = item_01 + item_02 + ... + item_16
mean = totalScore / 16
displayMean = round(mean, 2)
```

Category dùng `mean` chưa làm tròn:

| Mean | Key | Nhãn |
|---:|---|---|
| 1.00–2.33 | `low` | Thấp |
| 2.34–3.66 | `moderate` | Trung bình |
| 3.67–5.00 | `high` | Cao |

Các range này chỉ là phản hồi giáo dục tạm thời, không phải cutoff lâm sàng hoặc chuẩn quốc tế.

### Error response

| HTTP status | Error code | Trường hợp |
|---:|---|---|
| `400` | `INVALID_REQUEST` | JSON sai hoặc thiếu trường bắt buộc |
| `404` | `TEST_NOT_FOUND` | Không tìm thấy bài test |
| `409` | `TEST_VERSION_MISMATCH` | Sai phiên bản bài test |
| `422` | `INVALID_ANSWER_COUNT` | Không đủ hoặc thừa 16 câu |
| `422` | `DUPLICATE_QUESTION_ID` | Trùng câu hỏi |
| `422` | `UNKNOWN_QUESTION_ID` | Câu hỏi không thuộc test version |
| `422` | `INVALID_LIKERT_VALUE` | Giá trị không nằm trong 1–5 |
| `500` | `INTERNAL_ERROR` | Lỗi hệ thống |

Error body thống nhất:

```json
{
  "error": {
    "code": "INVALID_ANSWER_COUNT",
    "message": "Bài test yêu cầu đủ 16 câu trả lời.",
    "details": []
  }
}
```

Rate limit khi được triển khai sẽ sử dụng HTTP `429 Too Many Requests`.

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


## 6. Database và dữ liệu nội dung

PostgreSQL là database chính và sẽ được host trên Supabase. Dữ liệu nội dung cần được quản lý gồm:

- Câu hỏi và thứ tự hiển thị.
- Phiên bản bài test và locale.
- Nhóm đo (`emotional`, `functional`, `cognitive`, `control`).
- Các lựa chọn thuộc từng câu hỏi.
- Giá trị Likert từ 1 đến 5 của từng lựa chọn.
- Quy tắc chấm điểm và mức đánh giá theo khoảng điểm.
- Nhận xét chung và danh sách lời khuyên tương ứng.

Phase 1 không cần bảng `attempts` hoặc bảng lưu lịch sử kết quả cá nhân. Nếu backend lưu mẫu `advice` trong PostgreSQL bằng JSONB, đó là chi tiết nội bộ; API vẫn phải trả về `advice` dưới dạng mảng chuỗi JSON.

README frontend định hướng chuẩn bị nội dung bằng file Excel rồi import vào PostgreSQL thông qua backend. Schema, migration framework, format Excel, seed data và lệnh import **chưa được định nghĩa trong repository hiện tại**; cần bổ sung trước khi hướng dẫn vận hành database.

`compose.yaml` hiện có service PostgreSQL mẫu với database/user/password mẫu và port container `5432`. Đây chỉ là cấu hình phát triển ban đầu, không dùng thông tin mẫu này cho môi trường production. Cấu hình datasource local và Supabase chưa được hoàn thiện.

## 7. Cấu hình môi trường

Hiện chưa có `.env.example` và chưa có danh sách biến môi trường được chuẩn hóa. Khi cấu hình datasource, cần bổ sung tài liệu cho các nhóm giá trị sau mà không commit secret:

- JDBC URL PostgreSQL.
- Username và password database.
- Hibernate/JPA profile phù hợp với local và production.
- Port ứng dụng nếu khác mặc định.
- Rate-limit configuration.
- CORS origin của frontend Vercel và local.

Secret của Supabase, Render và database phải được lưu trong secret/environment settings của môi trường chạy, không đặt trong source code hoặc commit vào repository.

## 8. Rate limiter

Rate limiter là yêu cầu bắt buộc của backend nhưng **chưa được triển khai trong code hiện tại**. Thiết kế cần chốt trước khi implement:

- Giới hạn theo IP, theo route, hoặc kết hợp cả hai.
- Cửa sổ thời gian và số request tối đa.
- Các endpoint áp dụng, tối thiểu là endpoint lấy quiz và submit quiz.
- Cách xử lý proxy của Render để lấy client IP an toàn.
- Response `429 Too Many Requests`, header liên quan và thông tin retry nếu có.
- Storage của counter trong môi trường nhiều instance; không nên chỉ dùng memory local nếu Render scale nhiều instance.

Rate limiter không thay thế validation, giới hạn payload và các biện pháp bảo vệ database.

## 9. Kiểm thử và kiểm tra chất lượng

Test hiện tại là Spring Boot context test trong `BackendApplicationTests`. Chạy bằng:

```bash
./mvnw test
```

Khi API được triển khai, cần bổ sung test cho:

- Lấy đúng 16 câu hỏi theo thứ tự cố định.
- Mỗi câu có đúng 5 lựa chọn Likert từ 1 đến 5.
- Submit đủ 16 câu và tính `totalScore`, `mean`, `displayMean` đúng.
- Kiểm tra category ranges dựa trên `mean` chưa làm tròn.
- Kiểm tra 4 subscale và key `control` với label “Phụ thuộc về kiểm soát”.
- Kiểm tra `feedback` và `advice[]` trong response.
- Thiếu câu trả lời, duplicate `questionId`, `questionId` sai và giá trị Likert sai.
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

## 10. Deployment

Render là host backend dự kiến; Supabase là host PostgreSQL dự kiến. Chi tiết deploy chưa được cấu hình trong repository, vì vậy các giá trị sau vẫn cần xác định trên Render:

- Build command và start command chính thức.
- Java runtime/build image.
- Environment variables production.
- Health check endpoint.
- CORS origin của frontend production.
- Migration/seed strategy khi deploy.
- Log, monitoring và rollback procedure.

Không đưa credential Supabase vào README. Sau khi có contract và cấu hình deploy, cập nhật phần này bằng các lệnh thực tế đã kiểm tra trên Render.

## 11. Troubleshooting

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

## 12. Đóng góp

Trước khi mở pull request:

1. Cập nhật API contract nếu thay đổi endpoint hoặc schema.
2. Bổ sung test cho use case và error case mới.
3. Chạy `./mvnw test` và `./mvnw package`.
4. Cập nhật README này nếu thay đổi cách chạy, cấu hình, database hoặc deployment.
5. Không commit secret, credential hoặc dữ liệu production.
