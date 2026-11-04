# AIDepQuiz — AI Dependence Self-Assessment

AIDepQuiz là website giáo dục bằng tiếng Việt giúp người dùng suy ngẫm về cách họ dựa vào trí tuệ nhân tạo trong học tập, công việc và đời sống. Phase 1 gồm một bài tự đánh giá 16 câu; frontend và backend nằm trong hai thư mục/repository riêng.

> **Giới hạn sử dụng:** AIDepQuiz là công cụ tự đánh giá mang tính giáo dục, không phải chẩn đoán tâm lý, không xác định “nghiện AI”, không đánh giá năng lực học tập và không dùng để đưa ra quyết định có ảnh hưởng lớn đến người làm bài. Phiên bản tiếng Việt là bản thử nghiệm lấy cảm hứng từ AIDep-22, không phải AIDep-22 gốc và chưa được kiểm định như một thang đo tại Việt Nam.



## Mục lục

- [Mục tiêu và nguyên tắc](#mục-tiêu-và-nguyên-tắc)
- [Phạm vi Phase 1](#phạm-vi-phase-1)
- [Cách trả lời và chấm điểm](#cách-trả-lời-và-chấm-điểm)
- [Ứng dụng và kiến trúc](#ứng-dụng-và-kiến-trúc)
- [API contract](#api-contract)
- [Cấu trúc repository](#cấu-trúc-repository)
- [Thiết lập và chạy](#thiết-lập-và-chạy)
- [Database và dữ liệu](#database-và-dữ-liệu)
- [Trạng thái triển khai](#trạng-thái-triển-khai)
- [Deployment và môi trường](#deployment-và-môi-trường)
- [Kiểm thử và tiêu chí chất lượng](#kiểm-thử-và-tiêu-chí-chất-lượng)
- [Đạo đức, quyền riêng tư và khả năng tiếp cận](#đạo-đức-quyền-riêng-tư-và-khả-năng-tiếp-cận)
- [Quy trình kiểm định học thuật](#quy-trình-kiểm-định-học-thuật)
- [Lộ trình](#lộ-trình)
- [Tài liệu và nguồn tham khảo](#tài-liệu-và-nguồn-tham-khảo)
- [Đóng góp](#đóng-góp)



## Mục tiêu và nguyên tắc



### Mục tiêu

- Giúp người dùng nhận biết khi nào AI hỗ trợ họ và khi nào họ có thể đang giao quá nhiều việc suy nghĩ cho AI.
- Khuyến khích sử dụng AI tự chủ, có suy xét và không phán xét người dùng.
- Xây dựng một bài tự đánh giá có cơ sở lý thuyết, ghi lại nguồn, lý do chọn câu hỏi, cách chấm và giới hạn diễn giải.
- Công khai phiên bản câu hỏi và cập nhật kết luận sau expert review, phỏng vấn nhận thức và pilot-test.



### Nguyên tắc nội dung và sản phẩm

- Đo mức độ phụ thuộc do người dùng tự báo cáo; không suy ra phụ thuộc chỉ từ tần suất dùng AI.
- Mỗi item đo một hành vi hoặc cảm nhận chính, có ngữ cảnh rõ, ngắn gọn, trung tính và phù hợp với ngôn ngữ/văn hóa người trả lời.
- Không dùng từ ngữ quy kết như “lười”, “nghiện”, “hỏng” hay “bị AI điều khiển”.
- Không có đáp án đúng/sai. Điểm cao biểu thị mức phụ thuộc được tự báo cáo cao hơn, không phải đánh giá con người.
- Backend là nguồn tính điểm/category/feedback/advice; frontend chỉ thu câu trả lời và hiển thị response.
- Theo contract Phase 1, không cần đăng nhập, không nhận `attemptId` và không lưu lịch sử kết quả cá nhân.



## Phạm vi Phase 1

Phase 1 chỉ có bài test **AI Dependence**, phiên bản `vi-dependence-v1`, gồm 16 câu Likert bằng tiếng Việt, chia thành bốn nhóm với bốn câu mỗi nhóm. Đối tượng hướng đến là người dùng Việt Nam, ưu tiên học sinh/sinh viên theo phạm vi nghiên cứu của trường.


| Key          | Nhãn hiển thị          | Khía cạnh tham khảo                                  |
| ------------ | ---------------------- | ---------------------------------------------------- |
| `emotional`  | Phụ thuộc về cảm xúc   | Bất an, căng thẳng hoặc thiếu tự tin khi không có AI |
| `functional` | Phụ thuộc về chức năng | Khó hoàn thành nhiệm vụ nếu thiếu AI                 |
| `cognitive`  | Phụ thuộc về nhận thức | Giao suy nghĩ, phân tích hoặc quyết định cho AI      |
| `control`    | Mất khả năng kiểm soát | Khó giảm hoặc dừng sử dụng AI dù muốn                |


Đây là phiên bản rút gọn thử nghiệm lấy cảm hứng từ bốn nhóm của AIDep-22. AIDep-22 được nghiên cứu trên sinh viên đại học Trung Quốc; kết quả đó không tự động đại diện cho học sinh hoặc người dùng Việt Nam. Phase 1 không đo AI literacy tổng quát. Câu hỏi kiến thức hoặc tình huống về privacy, bias và fact-checking không được cộng vào điểm bài test này; các chủ đề đó có thể được xem xét cho phase sau.

Khi biên soạn item, mỗi câu cần đo một khái niệm chính, diễn đạt ngắn và trung tính, không gộp nhiều hành vi và không yêu cầu nhớ lại một khoảng thời gian cố định như “30 ngày qua”. Ghi lại construct, nguồn tham khảo và lý do chọn/chỉnh sửa item. Migration hiện tại chưa lưu đầy đủ các metadata nghiên cứu này.

## Cách trả lời và chấm điểm



### Thang trả lời


| Giá trị | Mức độ đồng ý                    |
| ------- | -------------------------------- |
| 1       | Hoàn toàn không đồng ý           |
| 2       | Không đồng ý                     |
| 3       | Không đồng ý cũng không phản đối |
| 4       | Đồng ý                           |
| 5       | Hoàn toàn đồng ý                 |


Mỗi câu là một phát biểu ở ngôi thứ nhất. Submit phải có câu trả lời hợp lệ cho cả 16 câu; không chấm bài có câu bỏ trống. `vi-dependence-v1` không có item đảo chiều (`reverse_scored = false` cho cả 16 câu).

### Công thức

```text
totalScore   = sum(item_01 ... item_16)       # 16–80
mean         = totalScore / 16                # 1.00–5.00
subscaleMean = sum(4 items in a group) / 4
```

`mean` chưa làm tròn là chỉ số phân loại chính. Điểm trung bình toàn bài hiển thị hai chữ số thập phân; điểm tổng là số nguyên và chỉ là thông tin bổ sung. Lưu ý: contract Backend hiện yêu cầu `subscales[].displayMean` có đúng hai chữ số (ví dụ `3.50`), trong khi contract Frontend cho phép bỏ số 0 cuối (ví dụ `3.5`). Đây là khác biệt cần được giải quyết giữa hai contract trước khi tích hợp.

### Nhóm kết quả tạm thời


| Mean chưa làm tròn | Key        | Nhãn       |
| ------------------ | ---------- | ---------- |
| 1.00–2.33          | `low`      | Thấp       |
| 2.34–3.66          | `moderate` | Trung bình |
| 3.67–5.00          | `high`     | Cao        |


Các category này là phản hồi giáo dục tạm thời trước khi có dữ liệu pilot, không phải cutoff quốc tế, chuẩn lâm sàng hoặc chẩn đoán. Không dùng nhãn “nghiện AI”, “não bị AI chiếm quyền” hoặc “suy giảm trí tuệ”. Frontend không tự tính điểm, category hay ngưỡng. Nếu phiên bản tương lai thêm item đảo chiều, đảo bằng `reversedValue = 6 - originalValue` và tăng `testVersion`.

## Ứng dụng và kiến trúc


| Thành phần      | Công nghệ trong repository                                        | Trách nhiệm                                                                                      |
| --------------- | ----------------------------------------------------------------- | ------------------------------------------------------------------------------------------------ |
| Frontend        | Next.js App Router, React, TypeScript                             | Homepage, luồng quiz, request API và render kết quả                                              |
| Backend         | Java 21, Spring Boot 4.1.2, Spring MVC, Spring Data JPA/Hibernate | Theo contract: cung cấp quiz, xác thực câu trả lời, tính điểm và tạo kết quả                     |
| Cơ sở dữ liệu   | PostgreSQL; Supabase là môi trường dự kiến                        | Lưu cấu hình phiên bản bài test và nội dung quiz/result; không lưu attempt cá nhân trong Phase 1 |
| Hosting dự kiến | Vercel (FE), Render (BE), Supabase (PostgreSQL)                   | Chưa có cấu hình triển khai đã xác nhận trong repository                                         |


Luồng kiến trúc theo contract (controller/service BE còn thiếu trong code hiện tại):

```mermaid
flowchart LR
    U[Người dùng] --> FE[Next.js frontend]
    FE -->|GET quiz / POST answers| API[Spring Boot REST API theo contract]
    API --> JPA[Service và JPA — cần hoàn thiện]
    JPA --> DB[(PostgreSQL / Supabase)]
    API -->|Quiz definition / result response| FE
    FE -->|Lưu response trong phiên hiện tại| R[/result_quiz]
```





### Luồng trang


| Route          | Vai trò                                                                             |
| -------------- | ----------------------------------------------------------------------------------- |
| `/`            | Homepage gồm hero và các section thông tin; menu cuộn nội bộ đến section            |
| `/quiz`        | Làm bài một câu mỗi lần, hiển thị `Câu x/16`, tiến trình, nút quay lại/tiếp tục     |
| `/result_quiz` | Hiển thị kết quả từ response submit; đây là route frontend, không phải API endpoint |


Chỉ nút bắt đầu bài test điều hướng từ `/` sang `/quiz`; menu nội dung trên homepage dùng anchor cuộn đến `#quiz-info`, `#academic-basis` và `#test-result-security`. Trong quiz, câu trả lời được giữ trong phiên làm bài khi đi tới/lùi; không hiện điểm trong lúc trả lời. Sau submit thành công, frontend giữ response trong phiên hiện tại rồi chuyển sang `/result_quiz`. Truy cập trực tiếp hoặc tải lại trang kết quả khi không còn phiên kết quả sẽ quay về `/`. Thử lại yêu cầu xác nhận trước khi xóa kết quả phiên hiện tại.

### Nguyên tắc giao diện và tương tác

- Giữ ngôn ngữ trung tính, không gây xấu hổ hoặc tạo cảm giác cảnh báo bằng màu đỏ.
- Hỗ trợ desktop, tablet và mobile; menu desktop chuyển thành drawer trên màn hình nhỏ. Navigation chính sticky ở đầu viewport.
- Drawer đóng bằng nút đóng, backdrop hoặc `Escape`; liên kết điều hướng đóng drawer rồi cuộn đến đúng section.
- Dùng Montserrat cho headings và Inter cho nội dung, navigation, controls. Các vai trò typography chung: navigation 15px Inter Regular; logo 15px Inter Extra Bold; title chính 35px Montserrat Bold; mô tả title chính 20px Inter Regular; title phụ 20px Montserrat SemiBold; mô tả/card text 18px Inter Regular; button/link 13px Inter Regular.
- Màu trạng thái dùng trong thiết kế gồm cyan `#C1F4FF` cho tiến trình đã trả lời, `#F4F3F3` cho phần còn lại, `#4D4D4D` cho nút bị vô hiệu hóa và `#8F8B8B` cho secondary hover. Cỡ chữ và layout chi tiết nằm trong tài liệu UI.
- Hỗ trợ bàn phím, focus hiển thị rõ và `prefers-reduced-motion`; không phụ thuộc vào màu sắc đơn thuần để truyền thông tin.
- Loading và lỗi API có trạng thái riêng, thông điệp an toàn và thao tác thử lại. Không dùng mock response khi API không khả dụng.
- Trang kết quả hiển thị điểm chung, bốn nhóm, feedback, 1–3 lời khuyên và giới hạn diễn giải qua nội dung/disclaimer phù hợp.
- Homepage section chính là `#quiz-info`, `#academic-basis` và `#test-result-security`; nút “Bắt đầu tự đánh giá” mở `/quiz`, nút “Xem phương pháp” cuộn đến `#academic-basis`.
- Quiz chỉ hiển thị một statement mỗi lần; nút tiếp tục chưa hoạt động cho đến khi chọn một đáp án. Nhóm câu đổi tại ranh giới câu `4→5`, `8→9`, `12→13`.
- Result gồm điểm chung, bốn nhóm, feedback, 1–3 lời khuyên và disclaimer ngắn. Nút thử lại mở hộp thoại xác nhận; hủy giữ kết quả hiện tại.
- Quy cách component, typography và chuyển động chi tiết được ghi trong [đặc tả tương tác](Frontend/docs/interaction-spec.md) và [tài liệu UI](Frontend/docs/ui-components.md). Nếu chi tiết giao diện thay đổi, cập nhật tài liệu UI cùng code.



## API contract

Contract API của repository Backend nằm tại [`docs/preparation/api-contract.yaml`](docs/preparation/api-contract.yaml) (OpenAPI 3.0.3, base path `/api/v1`). Contract frontend tương ứng nằm ở repository sibling `../Frontend/api-contract.yaml`; hai contract hiện chưa thống nhất hoàn toàn. Cả hai contract trả `subscale` (key) và `subscaleLabel` (chuỗi hiển thị) cho từng câu hỏi, để giao diện hiện tên khía cạnh phù hợp với câu đang xem. Nhãn `control` thống nhất là “Mất khả năng kiểm soát”; điểm `subscales[].displayMean` cho phép một hoặc hai chữ số sau dấu thập phân. Contract Backend và Frontend được đồng bộ theo các quy tắc này.

### Dữ liệu DB cho hai màn hình

Với schema hiện tại, UC01 có thể dựng nội dung bằng cách lấy version đang active, join `questions` với `subscales` để trả `subscale` (key) cùng `subscaleLabel` (chuỗi hiển thị), rồi join `options` để trả `optionId`, `value` và `text`; sắp xếp câu hỏi theo `questions.display_order`, lựa chọn theo `options.display_order`. `scale_labels` cung cấp nhãn chung Likert 1–5. Vì mỗi câu gắn trực tiếp với một subscale, FE nhận nhãn phù hợp theo từng câu/range mà không tự đoán từ số câu.

UC02 tính `totalScore`, `mean` và bốn subscale mean từ giá trị trả lời; chọn một dòng `results` theo version/category range để lấy category, description, feedback, `advice` và `limitations`. Các giá trị tổng/điểm nhóm được tính theo request, không cần bảng attempt. Screenshot kết quả có ghi điểm minh họa; không lưu các điểm đó thành dữ liệu seed.

### UC01 — Tải bài test

```http
GET /api/v1/tests/ai-dependence
```

Response cung cấp `testSlug`, `testVersion`, `locale`, title, đúng 16 câu theo thứ tự cố định, mỗi câu có đúng 5 lựa chọn, và quy tắc chấm điểm. Frontend render câu hỏi/lựa chọn từ response, không tự thay nội dung.

### UC02 — Gửi bài và nhận kết quả

```http
POST /api/v1/tests/ai-dependence/submit
Content-Type: application/json
```

Request mẫu dưới đây được rút gọn để thể hiện cấu trúc; request thật phải gồm đủ 16 câu trả lời duy nhất:

```json
{
  "testVersion": "vi-dependence-v1",
  "answers": [
    { "questionId": "dep-01", "value": 4 },
    { "questionId": "dep-02", "value": 3 }
  ]
}
```

Backend kiểm tra số câu, tính hợp lệ và tính điểm. Client không gửi `testSlug`, `totalScore`, `mean`, `category`, `feedback`, `advice` hoặc `limitations`.

Response mẫu:

```json
{
  "testSlug": "ai-dependence",
  "testVersion": "vi-dependence-v1",
  "locale": "vi-VN",
  "score": {
    "totalScore": 53,
    "maxTotalScore": 80,
    "mean": 3.3125,
    "displayMean": "3.31"
  },
  "category": {
    "key": "moderate",
    "label": "Trung bình",
    "description": "Mức phụ thuộc được tự báo cáo ở mức trung gian."
  },
  "subscales": [
    { "key": "emotional", "label": "Phụ thuộc về cảm xúc", "mean": 3.25, "displayMean": "3.25" },
    { "key": "functional", "label": "Phụ thuộc về chức năng", "mean": 3.5, "displayMean": "3.5" },
    { "key": "cognitive", "label": "Phụ thuộc về nhận thức", "mean": 3.0, "displayMean": "3.0" },
    { "key": "control", "label": "Mất khả năng kiểm soát", "mean": 3.5, "displayMean": "3.5" }
  ],
  "feedback": "Bạn đang sử dụng AI khá thường xuyên trong một số hoạt động.",
  "advice": [
    "Hãy thử tự phác thảo hướng giải quyết trước khi hỏi AI.",
    "Kiểm tra lại thông tin quan trọng từ nguồn đáng tin cậy."
  ],
  "limitations": [
    "Kết quả dựa trên tự báo cáo.",
    "Đây là phiên bản thử nghiệm tiếng Việt gồm 16 câu hỏi.",
    "Các mức phân loại chỉ nhằm mục đích giáo dục và tự nhận thức.",
    "Kết quả không phải là chẩn đoán tâm lý."
  ]
}
```



### Luồng xử lý FE


| Sự kiện                   | Hành động                                                 |
| ------------------------- | --------------------------------------------------------- |
| Tải quiz thành công       | Render câu đầu tiên từ định nghĩa API                     |
| Chọn đáp án               | Cập nhật câu trả lời và progress trong phiên hiện tại     |
| Submit thành công (`200`) | Lưu response trong phiên hiện tại rồi mở `/result_quiz`   |
| Submit thất bại           | Giữ trạng thái quiz, hiện lỗi an toàn và cho phép thử lại |
| Xác nhận làm lại          | Xóa kết quả phiên hiện tại và bắt đầu quiz mới            |




### Mã lỗi contract


| HTTP | Code                    | Trường hợp                          |
| ---- | ----------------------- | ----------------------------------- |
| 400  | `INVALID_REQUEST`       | JSON sai hoặc thiếu trường bắt buộc |
| 404  | `TEST_NOT_FOUND`        | Không tìm thấy bài test             |
| 409  | `TEST_VERSION_MISMATCH` | Phiên bản test không khớp           |
| 422  | `INVALID_ANSWER_COUNT`  | Không đủ hoặc thừa 16 câu           |
| 422  | `DUPLICATE_QUESTION_ID` | Lặp ID câu hỏi                      |
| 422  | `UNKNOWN_QUESTION_ID`   | ID không thuộc phiên bản test       |
| 422  | `INVALID_LIKERT_VALUE`  | Giá trị ngoài 1–5                   |
| 500  | `INTERNAL_ERROR`        | Lỗi hệ thống                        |


Lỗi có cấu trúc `{ "error": { "code", "message", "details": [] } }`. UI cần ánh xạ lỗi sang thông điệp thân thiện, không lộ stack trace hoặc chi tiết nội bộ. Rate limiting và `429` chưa nằm trong contract.

## Cấu trúc repository

```text
AIDepQuiz/                         # thư mục làm việc tổng hợp; chưa phải Git root
├── README.md                      # tài liệu tổng quan FE + BE
├── Frontend/                      # Git repository riêng
│   ├── app/                       # route /, /quiz, /result_quiz
│   ├── components/                # homepage, navigation, quiz, result, motion
│   ├── lib/                       # API client và kiểu dữ liệu
│   ├── docs/                       # feature flow, interaction, UI components
│   ├── api-contract.yaml           # contract API chuẩn
│   └── package.json
└── Backend/                       # Git repository riêng
    ├── src/main/java/              # Spring Boot application và JPA entities
    ├── src/main/resources/         # application.properties
    ├── src/test/java/              # Spring Boot context test hiện có
    ├── docs/preparation/           # contract API bản BE (cần đồng bộ)
    ├── supabase/migrations/        # schema PostgreSQL
    ├── compose.yaml                # PostgreSQL mẫu
    └── pom.xml
```

Các thư mục trên là cấu trúc hiện có đã kiểm tra, không bao gồm `node_modules`, build output hay file cục bộ. `Frontend/` và `Backend/` có Git root riêng; thư mục cha chưa có monorepo, CI chung hoặc quản lý dependency chung.

## Thiết lập và chạy



### Frontend

Yêu cầu Node.js và npm. Từ `Frontend/`:

```bash
npm install
npm run dev
```

Scripts hiện có trong `Frontend/package.json`:

```bash
npm run lint
npm run typecheck
npm run build
npm run start
```

`npm run start` phục vụ production build đã tạo bằng `npm run build`.

API client đọc `NEXT_PUBLIC_API_BASE_URL`; mặc định dùng `/api/v1`. Khi API ở một origin khác, đặt biến này thành base URL có hậu tố `/api/v1`. FE hiện không định nghĩa proxy/rewrite trong `next.config.ts`; CORS và URL kết nối phải được cấu hình khi BE có endpoint hoạt động.

### Backend

Yêu cầu JDK 21, PostgreSQL có thể truy cập và Maven Wrapper. `Backend/src/main/resources/application.properties` yêu cầu các biến sau:


| Biến môi trường          | Ý nghĩa                 |
| ------------------------ | ----------------------- |
| `SUPABASE_DB_DATASOURCE` | JDBC URL PostgreSQL     |
| `SUPABASE_DB_USERNAME`   | Tên người dùng database |
| `SUPABASE_DB_PASSWORD`   | Mật khẩu database       |


Đặt biến trong môi trường chạy, không commit credentials. Hibernate dùng `ddl-auto=validate`: ứng dụng không tự tạo schema và cần schema khớp entity mapping. Từ `Backend/`:

```bash
./mvnw test
./mvnw spring-boot:run
./mvnw package
```

Trên Windows dùng `mvnw.cmd`. Các lệnh cần datasource PostgreSQL hợp lệ. Spring Docker Compose integration đang bị tắt (`spring.docker.compose.enabled=false`); `compose.yaml` là PostgreSQL mẫu, phải khởi động riêng và không dùng thông tin mẫu ngoài môi trường phát triển.

> Hiện entity mapping chưa khớp đầy đủ migration. Vì vậy, không giả định ứng dụng hoặc Spring context test khởi động thành công với migration hiện tại cho đến khi mapping và schema được đồng bộ.



## Database và dữ liệu nội dung

Migration PostgreSQL hiện có tại `[Backend/supabase/migrations/20261102084017_database_schema_ai_dep_quiz.sql](Backend/supabase/migrations/20261102084017_database_schema_ai_dep_quiz.sql)` định nghĩa:


| Bảng            | Vai trò                                                     |
| --------------- | ----------------------------------------------------------- |
| `test_versions` | Slug, version, locale, số câu, thang đo và scoring rules    |
| `scale_labels`  | Nhãn Likert 1–5                                             |
| `subscales`     | Bốn nhóm đo và thứ tự hiển thị                              |
| `questions`     | ID nghiệp vụ, câu hỏi, nhóm và thứ tự                       |
| `options`       | Lựa chọn Likert theo câu hỏi                                |
| `results`       | Category range, feedback, advice JSONB và limitations JSONB |


Migration không tạo `attempts` hay bảng lịch sử kết quả cá nhân. Seed data, công cụ migration được chạy trong production và quy trình import Excel chưa được cấu hình. `compose.yaml` dùng giá trị mẫu cho database/user/password.

Ba JPA entity `Question`, `Option`, `Result` đã tồn tại, nhưng hiện chưa bao quát đầy đủ cột/quan hệ của migration; `Result` cũng đang dùng tên/cấu trúc trường khác schema mới. Cần đồng bộ entities, repositories, services, seed data và migration trước khi persistence/API được xem là dùng được.

## Trạng thái triển khai


| Hạng mục                   | Hiện tại                                                                      |
| -------------------------- | ----------------------------------------------------------------------------- |
| FE routes và UI            | Có source cho `/`, `/quiz`, `/result_quiz`; có API client và TypeScript types |
| BE runtime API             | Chưa có controller, repository hoặc service triển khai các endpoint contract  |
| Persistence                | Có ba entity và migration, nhưng chưa đồng bộ hoàn toàn                       |
| API contract               | FE `api-contract.yaml` là chuẩn; bản tại BE còn sai khác                      |
| Rate limiting              | Chưa chốt và chưa triển khai; không nằm trong contract                        |
| Seed/import nội dung       | Chưa có                                                                       |
| Authentication/lưu kết quả | Không thuộc Phase 1 theo contract                                             |
| Deployment                 | Vercel/Render/Supabase là dự kiến; chưa có cấu hình deploy được xác nhận      |
| Nghiên cứu/validation      | Chưa đủ bằng chứng để gọi đây là thang đo đã kiểm định                        |


README này mô tả contract sản phẩm và trạng thái source; sự tồn tại của contract không có nghĩa API đã chạy được. Không dùng nội dung trong `Frontend/use-case.md` làm nguồn nghiệp vụ hiện hành: tài liệu đó còn mô tả bài test 10 câu và request `{questionId, optionId}`, đã lỗi thời so với contract 16 câu dùng `{questionId, value}`.

## Deployment và môi trường

Vercel (frontend), Render (backend) và Supabase (PostgreSQL) là các nền tảng dự kiến, không phải cấu hình đã xác nhận. Repository hiện chưa có workflow deploy/CI, URL production, start command Render, health check, cấu hình CORS, secrets, migration/seed khi deploy, monitoring hoặc rollback procedure. Không commit credentials và không điền URL production giả định vào README. Khi thiết lập từng môi trường, bổ sung chính xác các giá trị đã triển khai và kiểm tra.

## Kiểm thử và tiêu chí chất lượng



### Lệnh hiện có

Frontend:

```bash
npm run lint
npm run typecheck
npm run build
```

Backend:

```bash
./mvnw test
./mvnw package
```

Backend context test dùng cấu hình datasource ứng dụng; cần PostgreSQL và ba biến môi trường đã nêu. Test integration, migration và contract chưa được cấu hình đầy đủ. README không khẳng định các lệnh đã chạy thành công.

### Kiểm tra luồng frontend

- Điều hướng giữa các câu, giữ câu trả lời khi lùi/tiến và không cho hoàn thành khi còn câu bỏ trống.
- Hiển thị đúng progress, nhóm câu hỏi và trạng thái disabled của nút quay lại.
- Loading/error response có hướng dẫn thử lại; không dùng mock khi API lỗi.
- Chỉ điều hướng sang kết quả sau response submit thành công.
- Thử trực tiếp `/result_quiz`, refresh, xác nhận thử lại, hủy thử lại và quay về homepage.
- Kiểm tra responsive, keyboard, screen reader và reduced motion.



### Kiểm tra backend khi endpoints được triển khai

- Lấy đúng test version, 16 câu và 5 lựa chọn/câu theo thứ tự.
- Từ chối câu trả lời thiếu/thừa, ID trùng, ID lạ, version sai hoặc giá trị ngoài 1–5.
- Tính total, mean, nhóm và category đúng; phân loại trên mean chưa làm tròn.
- Không nhận điểm/category/feedback do client tự gửi.
- Trả response đầy đủ và đúng kiểu; ánh xạ lỗi theo status/code contract.
- Không yêu cầu authentication và không ghi dữ liệu cá nhân không cần thiết vào logs.



## Đạo đức, quyền riêng tư và khả năng tiếp cận

- Không yêu cầu đăng nhập Phase 1; không thu tên, email hoặc dữ liệu nhạy cảm nếu không cần.
- Nếu bổ sung thu thập dữ liệu nghiên cứu, cần thông báo mục đích, thời gian lưu, cách sử dụng và xin đồng thuận rõ ràng; với người chưa thành niên cần theo chính sách trường và quy định liên quan đến phụ huynh/người giám hộ.
- Chỉ thu thập dữ liệu tối thiểu; không lưu attempt/result cá nhân trong Phase 1 hiện tại.
- Dùng nội dung không phán xét; không tạo bảng xếp hạng hay so sánh nhóm khi chưa có bằng chứng phù hợp.
- Cho phép sử dụng bằng bàn phím và screen reader; duy trì focus hiển thị, responsive layout và reduced motion.
- Không dùng màu sắc là tín hiệu duy nhất; không dùng thiết kế hoặc lời khuyên để làm người dùng sợ hãi/xấu hổ.
- Cho phép người dùng đọc phương pháp, nguồn và giới hạn trước khi bắt đầu.



## Quy trình kiểm định học thuật

Trước khi diễn giải kết quả như một công cụ đo lường đã được xác nhận:

1. Xác định rõ nhóm người dùng mục tiêu (học sinh, sinh viên hay người đi làm).
2. Dịch và thích nghi câu hỏi có ghi nguồn và lý do chỉnh sửa.
3. Xin chuyên gia/giáo viên review độ rõ, tính phù hợp và nội dung.
4. Phỏng vấn nhận thức khoảng 5–10 người để biết họ hiểu item ra sao.
5. Pilot khoảng 30 người để phát hiện câu khó hiểu; mẫu này không tự nó xác nhận thang đo.
6. Với mẫu đủ lớn, xem phân phối câu trả lời, item-total correlation và Cronbach’s alpha hoặc McDonald’s omega.
7. Thực hiện EFA khi có đủ dữ liệu; không tuyên bố xác nhận cấu trúc nếu thiếu CFA hoặc mẫu độc lập.
8. Xem xét test–retest nếu cần đánh giá độ ổn định.
9. Không tạo chuẩn/cutoff quốc tế từ một mẫu trường học nhỏ.
10. Công khai giới hạn mẫu, bản dịch, độ tin cậy, giá trị đo lường và cách diễn giải.

Nếu mở rộng đa quốc gia, cần dịch xuôi/ngược, phỏng vấn người dùng bản địa, kiểm tra measurement invariance hoặc differential item functioning; không xếp hạng quốc gia khi chưa chứng minh thang đo tương đương.

## Lộ trình



### Phase 1 — Vietnamese AI Dependence MVP

- Homepage, thông tin bài test, cơ sở học thuật, nguồn và quyền riêng tư.
- Một bài test 16 câu Likert, bốn nhóm, tiếng Việt.
- Trả kết quả sau khi submit; không đăng nhập, không leaderboard, không lưu lịch sử cá nhân.
- Category và lời khuyên chỉ mang tính giáo dục tạm thời.



### Phase 2 — Research pilot

- Version hóa questionnaire và scoring.
- Chỉ lưu dữ liệu nghiên cứu ẩn danh khi có consent và quy trình bảo mật.
- Export dữ liệu phân tích; báo cáo độ tin cậy và item analysis.
- Sửa item theo kết quả pilot; đánh giá có cần phiên bản 22 câu hay không.
- Cân nhắc Test B về Responsible AI Awareness như sản phẩm riêng.



### Phase 3 — International adaptation

- Dịch và thích nghi văn hóa theo từng ngôn ngữ.
- Cognitive interview theo từng nhóm người dùng.
- Kiểm tra tương đương đo lường trước khi so sánh nhóm/quốc gia.
- Cải thiện accessibility dựa trên nghiên cứu người dùng.



## Tài liệu và nguồn tham khảo



### Tài liệu trong dự án

- [OpenAPI contract chuẩn](Frontend/api-contract.yaml)
- [Luồng tính năng và sự kiện nghiệp vụ](Frontend/docs/feature-flow.md)
- [Đặc tả tương tác](Frontend/docs/interaction-spec.md)
- [Tài liệu component và visual system](Frontend/docs/ui-components.md)
- [Migration PostgreSQL](Backend/supabase/migrations/20261102084017_database_schema_ai_dep_quiz.sql)
- [OpenAPI copy trong backend — cần đồng bộ](Backend/docs/preparation/api-contract.yaml)

`Frontend/use-case.md` là tài liệu cũ, ghi 10 câu và `optionId`; không dùng nó thay contract 16 câu hiện tại.

### Nguồn nghiên cứu và hướng dẫn

- [AIDep-22 — Development and validation of the AI Dependence Scale for Chinese undergraduates](https://www.frontiersin.org/journals/psychology/articles/10.3389/fpsyg.2025.1725393/full) — tham khảo bốn chiều và phát triển item; mẫu nghiên cứu là sinh viên đại học Trung Quốc.
- [AERA, APA & NCME — Standards for Educational and Psychological Testing](https://www.apa.org/science/programs/testing/standards) — validity, reliability, fairness và giới hạn diễn giải.
- [International Test Commission — Guidelines for Translating and Adapting Tests](https://www.intestcom.org/page/14) — dịch, thích nghi văn hóa và ghi nhận thay đổi item.
- [International Test Commission — Technology-Based Assessment](https://www.intestcom.org/page/16) — fairness, accessibility, security và privacy trong đánh giá số.
- [OECD/European Commission — Empowering Learners for the Age of AI](https://www.oecd.org/en/publications/empowering-learners-for-the-age-of-ai_65cd27d4-en.html)
- [UNESCO — AI Competency Framework for Students](https://www.unesco.org/en/articles/ai-competency-framework-students)
- [UNESCO — Guidance for Generative AI in Education and Research](https://www.unesco.org/en/articles/guidance-generative-ai-education-and-research)
- [European Commission — DigComp 3.0](https://joint-research-centre.ec.europa.eu/scientific-activities/key-competences-lifelong-learning/digital-competence-framework-digcomp/digcomp-30_en)
- [NIST AI Risk Management Framework](https://www.nist.gov/itl/ai-risk-management-framework)
- [NIST Generative AI Profile](https://www.nist.gov/publications/artificial-intelligence-risk-management-framework-generative-artificial-intelligence)

AIDep-22 và các hướng dẫn kiểm định/thích nghi là căn cứ trực tiếp cho bài test hiện tại. OECD, UNESCO, DigComp và NIST là bối cảnh cho các phase sau, không được dùng để cộng điểm cho 16 item Phase 1.

## Đóng góp

Khi thay đổi quiz, scoring hoặc API:

1. Cập nhật `Frontend/api-contract.yaml` và đồng bộ `Backend/docs/preparation/api-contract.yaml`.
2. Tăng `testVersion` nếu thay đổi câu hỏi hoặc quy tắc chấm.
3. Bổ sung test cho luồng chính, boundary score và lỗi validation.
4. Cập nhật README này khi thay đổi cách chạy, môi trường, schema, phạm vi hoặc trạng thái triển khai.
5. Chạy lint/typecheck/build của FE và test/package của BE khi đã cấu hình môi trường cần thiết.
6. Không commit credentials, dữ liệu production hoặc dữ liệu nghiên cứu có thể nhận dạng cá nhân.



## Tuyên bố sử dụng

> AIDepQuiz is an educational self-assessment tool designed to help people reflect on their relationship with AI. Results are self-reported and preliminary. They are not a clinical diagnosis or a definitive judgment of a person's abilities.

