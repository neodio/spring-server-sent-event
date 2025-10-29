# Meeting Room SSE API Documentation

## 📋 개요
회의실의 퇴실 체크리스트를 실시간으로 관리하는 Spring Boot REST API입니다.
Server-Sent Events (SSE)를 통해 실시간 업데이트를 제공합니다.

## 🚀 실행 방법
```bash
./gradlew bootRun
```

애플리케이션은 기본적으로 `http://localhost:8080`에서 실행됩니다.

## 📊 H2 Console
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (없음)

## 🔌 API 엔드포인트

### 1. 회의실 생성
```http
POST /api/meeting-rooms
Content-Type: application/json

{
  "name": "미팅룸1"
}
```

**응답:**
```json
{
  "id": 1,
  "name": "미팅룸1",
  "airConditionerOff": false,
  "tvOff": false,
  "lightOff": false,
  "trashCleaned": false
}
```

### 2. 전체 회의실 목록 조회
```http
GET /api/meeting-rooms
```

**응답:**
```json
[
  {
    "id": 1,
    "name": "미팅룸1",
    "airConditionerOff": false,
    "tvOff": false,
    "lightOff": false,
    "trashCleaned": false
  }
]
```

### 3. 특정 회의실 조회
```http
GET /api/meeting-rooms/{id}
```

**응답:**
```json
{
  "id": 1,
  "name": "미팅룸1",
  "airConditionerOff": false,
  "tvOff": false,
  "lightOff": false,
  "trashCleaned": false
}
```

### 4. 회의실 체크리스트 업데이트
```http
PATCH /api/meeting-rooms/{id}
Content-Type: application/json

{
  "airConditionerOff": true,
  "tvOff": true,
  "lightOff": false,
  "trashCleaned": false
}
```

**응답:**
```json
{
  "id": 1,
  "name": "미팅룸1",
  "airConditionerOff": true,
  "tvOff": true,
  "lightOff": false,
  "trashCleaned": false
}
```

**참고:** 원하는 필드만 업데이트할 수 있습니다. null인 필드는 업데이트되지 않습니다.

### 5. SSE 구독 (실시간 업데이트)
```http
GET /api/meeting-rooms/{id}/subscribe
Accept: text/event-stream
```

**SSE 이벤트:**

1. **연결 이벤트 (connect)**
```
event: connect
data: Connected to meeting room 1
```

2. **업데이트 이벤트 (update)**
```
event: update
data: {"id":1,"name":"미팅룸1","airConditionerOff":true,"tvOff":true,"lightOff":false,"trashCleaned":false}
```

## 💡 SSE 클라이언트 예제

### JavaScript (브라우저)
```javascript
const eventSource = new EventSource('http://localhost:8080/api/meeting-rooms/1/subscribe');

// 연결 이벤트
eventSource.addEventListener('connect', (event) => {
  console.log('Connected:', event.data);
});

// 업데이트 이벤트
eventSource.addEventListener('update', (event) => {
  const data = JSON.parse(event.data);
  console.log('Meeting room updated:', data);
  
  // UI 업데이트 로직
  updateUI(data);
});

// 에러 처리
eventSource.onerror = (error) => {
  console.error('SSE Error:', error);
  eventSource.close();
};
```

### cURL 테스트
```bash
# SSE 구독
curl -N http://localhost:8080/api/meeting-rooms/1/subscribe

# 다른 터미널에서 업데이트 요청
curl -X PATCH http://localhost:8080/api/meeting-rooms/1 \
  -H "Content-Type: application/json" \
  -d '{"airConditionerOff": true}'
```

## 📁 프로젝트 구조
```
src/main/java/com/example/sse/
├── config/
│   ├── DataInitializer.java      # 초기 데이터 생성
│   └── WebConfig.java             # CORS 설정
├── controller/
│   └── MeetingRoomController.java # REST API 컨트롤러
├── dto/
│   ├── MeetingRoomCreateRequest.java
│   ├── MeetingRoomUpdateRequest.java
│   └── MeetingRoomResponse.java
├── entity/
│   └── MeetingRoom.java           # JPA Entity
├── exception/
│   └── GlobalExceptionHandler.java # 예외 처리
├── repository/
│   └── MeetingRoomRepository.java # JPA Repository
└── service/
    └── MeetingRoomService.java    # 비즈니스 로직 및 SSE 관리
```

## 🔧 주요 기능

### Entity 필드
- `id`: 회의실 고유 ID
- `name`: 회의실 이름
- `airConditionerOff`: 에어컨 끄기 체크 여부
- `tvOff`: TV 끄기 체크 여부
- `lightOff`: 불 끄기 체크 여부
- `trashCleaned`: 쓰레기 정리 체크 여부

### SSE 특징
- **타임아웃**: 1시간 (3600초)
- **자동 재연결**: 클라이언트에서 구현 필요
- **멀티 클라이언트 지원**: 여러 클라이언트가 동일한 회의실을 구독 가능
- **자동 정리**: 연결 종료 시 emitter 자동 제거

## 🧪 테스트 시나리오

1. **회의실 생성 및 조회**
   ```bash
   # 회의실 생성
   curl -X POST http://localhost:8080/api/meeting-rooms \
     -H "Content-Type: application/json" \
     -d '{"name": "미팅룸4"}'
   
   # 전체 조회
   curl http://localhost:8080/api/meeting-rooms
   ```

2. **SSE 구독 및 실시간 업데이트 테스트**
   ```bash
   # 터미널 1: SSE 구독
   curl -N http://localhost:8080/api/meeting-rooms/1/subscribe
   
   # 터미널 2: 체크리스트 업데이트
   curl -X PATCH http://localhost:8080/api/meeting-rooms/1 \
     -H "Content-Type: application/json" \
     -d '{"airConditionerOff": true, "tvOff": true}'
   ```

3. **부분 업데이트 테스트**
   ```bash
   # 에어컨만 체크
   curl -X PATCH http://localhost:8080/api/meeting-rooms/1 \
     -H "Content-Type: application/json" \
     -d '{"airConditionerOff": true}'
   ```

## ⚠️ 주의사항
- SSE 연결은 1시간 타임아웃이 있으므로, 장시간 연결 시 재연결 로직이 필요합니다.
- H2 인메모리 DB를 사용하므로 애플리케이션 재시작 시 데이터가 초기화됩니다.
- 프로덕션 환경에서는 MySQL, PostgreSQL 등의 영구 DB를 사용하세요.

## 🔄 데이터 플로우
```
1. 클라이언트 → SSE 구독 (GET /api/meeting-rooms/{id}/subscribe)
2. 서버 → 연결 이벤트 전송 (connect)
3. 다른 클라이언트 → 체크리스트 업데이트 (PATCH /api/meeting-rooms/{id})
4. 서버 → DB 업데이트
5. 서버 → 모든 구독자에게 업데이트 이벤트 전송 (update)
6. 클라이언트 → UI 업데이트
```
