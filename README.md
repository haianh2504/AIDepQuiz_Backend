# AIDepQuiz – Backend

Backend REST API cho AIDepQuiz, ứng dụng giúp người dùng tự đánh giá mức độ phụ thuộc vào AI trong học tập và công việc. Backend chịu trách nhiệm cung cấp nội dung quiz, kiểm tra câu trả lời, tính điểm, phân loại và trả về nhận xét cùng lời khuyên.

> Tài liệu này mô tả backend Phase 1 theo README frontend và OpenAPI contract tại `docs/preparation/api-contract.yaml`. Repository hiện có Spring Boot, ba JPA entity và migration PostgreSQL; controller, repository, service, dữ liệu seed và API runtime chưa được triển khai đầy đủ.

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
- Rate limiting chưa có trong code hoặc OpenAPI contract hiện tại; chỉ bổ sung khi yêu cầu và chính sách giới hạn được xác định.

### Ngoài phạm vi Phase 1

- Đăng ký, đăng nhập, session hoặc phân quyền.
- Dashboard/API quản trị nội dung.
- Cá nhân hóa lời khuyên bằng AI theo thời gian thực.
- Lưu attempt, lịch sử kết quả hoặc dữ liệu để tính thống kê người dùng.
- Rate limiting; chính sách này chưa được chốt trong contract hiện tại.

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
- Maven không bắt buộc nếu dùng Maven Wrapper.
- PostgreSQL có thể truy cập được; cấu hình hiện tại dùng PostgreSQL/Supabase.
- Docker Desktop chỉ cần nếu chủ động chạy PostgreSQL từ `compose.yaml`.

### Cấu hình kết nối database

`src/main/resources/application.properties` yêu cầu các biến môi trường sau:

| Biến | Ý nghĩa |
|---|---|
| `SUPABASE_DB_DATASOURCE` | JDBC URL của PostgreSQL |
| `SUPABASE_DB_USERNAME` | Tên người dùng database |
| `SUPABASE_DB_PASSWORD` | Mật khẩu database |

Cung cấp các giá trị này trong môi trường chạy trước khi khởi động ứng dụng hoặc test. Hibernate đang dùng `ddl-auto=validate`, nên schema database phải tồn tại và khớp mapping JPA. Không commit thông tin kết nối thật.

### Cài đặt và chạy

Từ thư mục backend, chạy:

```bash
./mvnw test
./mvnw spring-boot:run
```

Trên Windows:

```bat
mvnw.cmd test
mvnw.cmd spring-boot:run
```

Các lệnh cần kết nối database theo cấu hình trên. Spring Docker Compose integration hiện bị tắt trong `application.properties`; nếu dùng `compose.yaml`, hãy khởi động PostgreSQL riêng và cung cấp JDBC URL cùng thông tin đăng nhập tương ứng.

## 4. Use cases và API chính thức

Contract OpenAPI 3.0.3 của bản backend nằm tại [`docs/preparation/api-contract.yaml`](docs/preparation/api-contract.yaml); contract được README frontend chỉ định làm chuẩn nằm ở `../Frontend/api-contract.yaml`. Hai file hiện chưa đồng bộ hoàn toàn: bản backend còn dùng nhãn nhóm `Phụ thuộc về kiểm soát` và định dạng điểm nhóm cố định hai chữ số, trong khi chuẩn frontend dùng `Mất khả năng kiểm soát` và bỏ số 0 thập phân cuối không cần thiết. Hãy theo contract frontend khi triển khai và đồng bộ bản sao backend trước khi dùng nó làm contract triển khai. Base path:

```text
/api/v1
```

Theo contract Phase 1, API không yêu cầu đăng nhập, không nhận `attemptId` và không lưu lịch sử kết quả cá nhân. Các endpoint trong contract là đặc tả; chúng chưa được triển khai trong controller/runtime của repository hiện tại.

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

Rate limiting không được mô tả trong contract hiện tại; nếu được bổ sung, cần cập nhật contract cùng response `429 Too Many Requests`.

## 5. Kiến trúc và trạng thái triển khai

Luồng dự kiến theo contract:

```text
Next.js frontend
        ↓ REST/JSON
Spring MVC controller (chưa triển khai)
        ↓
Application/service layer (chưa triển khai)
        ↓
Spring Data JPA + Hibernate
        ↓
PostgreSQL trên Supabase
```

Hiện repository có lớp khởi động Spring Boot và các entity `Question`, `Option`, `Result`. Controller, repository và service mới là package placeholder; API chưa thể được xem là đang hoạt động chỉ dựa trên OpenAPI contract.

## 6. Database và dữ liệu nội dung

PostgreSQL là database Phase 1; Supabase là môi trường hosting dự kiến. Migration hiện có tại [`supabase/migrations/20261102084017_database_schema_ai_dep_quiz.sql`](supabase/migrations/20261102084017_database_schema_ai_dep_quiz.sql) định nghĩa các bảng `test_versions`, `scale_labels`, `subscales`, `questions`, `options` và `results`. Schema không tạo bảng `attempts` hoặc lịch sử kết quả cá nhân, phù hợp với contract stateless.

Ba JPA entity đã có (`Question`, `Option`, `Result`), nhưng mapping chưa bao quát đầy đủ schema migration và một số trường chưa khớp cột trong migration. Vì vậy không nên xem persistence đã sẵn sàng để chạy với `ddl-auto=validate`. Seed data và quy trình nạp nội dung chưa có trong repository.

`compose.yaml` có PostgreSQL mẫu dành cho phát triển. Giá trị database/user/password trong đó chỉ là mẫu; không sử dụng cho môi trường production. Ứng dụng không tự bật Docker Compose integration vì `spring.docker.compose.enabled=false`.

## 7. Cấu hình môi trường

Datasource được cấu hình trong `src/main/resources/application.properties` bằng các biến `SUPABASE_DB_DATASOURCE`, `SUPABASE_DB_USERNAME` và `SUPABASE_DB_PASSWORD`. Dùng JDBC URL PostgreSQL hợp lệ và thông tin đăng nhập của database tương ứng; giữ secret trong environment settings hoặc secret store, không đặt trong source code hay README.

Hibernate hiện dùng `ddl-auto=validate`, vì vậy ứng dụng không tự tạo hoặc cập nhật schema. Cần hoàn thiện migration, entity mapping và dữ liệu nội dung trước khi chạy ứng dụng với database thật. CORS origin và biến cấu hình rate limit chưa được khai báo trong repository.

## 8. Rate limiter

Rate limiter chưa được triển khai và không nằm trong contract OpenAPI hiện tại. Nếu được bổ sung, cần xác định ngưỡng, cửa sổ thời gian, endpoint áp dụng, xử lý IP phía sau proxy và phản hồi `429 Too Many Requests` trước khi ghi thành hướng dẫn vận hành.

## 9. Kiểm thử và kiểm tra chất lượng

Repository hiện có một Spring Boot context test tại `BackendApplicationTests`; test cần các biến datasource đã nêu ở phần Quick start vì cấu hình ứng dụng kết nối PostgreSQL. Chạy bằng:

```bash
./mvnw test
```

Khi API được triển khai, cần bổ sung test cho:

- Lấy đúng 16 câu hỏi theo thứ tự cố định.
- Mỗi câu có đúng 5 lựa chọn Likert từ 1 đến 5.
- Submit đủ 16 câu và tính `totalScore`, `mean`, `displayMean` đúng.
- Kiểm tra category ranges dựa trên `mean` chưa làm tròn.
- Kiểm tra 4 subscale, key `control`, nhãn “Mất khả năng kiểm soát” và định dạng `displayMean` theo contract frontend.
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

Các test integration, migration và contract test chưa được cấu hình trong repository hiện tại. Những mục kiểm tra API, scoring và rate limiting trong danh sách trên là yêu cầu cần bổ sung, không phải coverage hiện có.

## 10. Deployment

Render là backend host dự kiến và Supabase là PostgreSQL host dự kiến. Repository hiện chưa có cấu hình deploy hoặc CI/CD, nên chưa có build/start command, health check, migration/seed strategy hay rollback procedure đã xác nhận. Chỉ bổ sung các hướng dẫn deploy sau khi cấu hình thực tế được thiết lập và kiểm tra.

## 11. Troubleshooting

### Ứng dụng không khởi động do database

Kiểm tra PostgreSQL có thể truy cập, ba biến datasource được đặt đúng và JDBC URL cùng thông tin đăng nhập hợp lệ. `ddl-auto=validate` yêu cầu schema đã tồn tại và khớp entity mapping; giá trị mẫu trong `compose.yaml` không phải cấu hình production.

### Test context thất bại

Chạy lại:

```bash
./mvnw test
```

Sau đó kiểm tra log Spring Boot, datasource và schema. Context test hiện dùng cấu hình ứng dụng nên cần database PostgreSQL phù hợp; repository chưa cấu hình test database độc lập.

### Frontend không gọi được backend

Kiểm tra API contract, backend URL, CORS origin và response error thực tế. Contract mô tả endpoint dự kiến; controller/API runtime hiện chưa có trong repository.

## 12. Đóng góp

Trước khi mở pull request:

1. Cập nhật API contract nếu thay đổi endpoint hoặc schema.
2. Bổ sung test cho use case và error case mới.
3. Cung cấp cấu hình datasource cần thiết, sau đó chạy `./mvnw test` và `./mvnw package`.
4. Cập nhật README này nếu thay đổi cách chạy, cấu hình, database hoặc deployment.
5. Không commit secret, credential hoặc dữ liệu production.
