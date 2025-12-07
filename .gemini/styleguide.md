# 찐빵(JJinBBang) 백엔드 스타일 가이드 & 컨벤션

## 1. 아키텍처 패턴 (Architectural Patterns)

- **프레임워크**: Spring Boot 3.x (Java 21)
- **계층형 아키텍처**: Controller -> Service (Interface + Impl) -> Repository (JPA + QueryDSL)
- **패키지 구조**: 도메인 주도 (`app.domain.{기능}`) + 전역 공통 (`app.global`)

## 2. 네이밍 컨벤션 (Naming Conventions)

- **클래스**: PascalCase (예: `UsersController`, `BuildingsRepository`)
- **메서드**: camelCase (예: `getUserInfo`, `findMarkersWithinBounds`)
- **변수**: camelCase
- **REST API**: URL 경로는 소문자 camelCase 사용 (예: `/api/v1/user/unregisterReason`)
- **DB 테이블**: Snake_case (JPA 네이밍 전략에 따름)

## 3. 클래스별 역할 및 규칙 (Class Responsibilities)

### Controller (`...Controller.java`)

- **어노테이션**: `@RestController`, `@RequestMapping("/api/v1/...")`, `@RequiredArgsConstructor` 필수.
- **응답 형식**: 항상 `ResTemplate<T>` 래퍼 클래스로 감싸서 반환.
- **검증(Validation)**: Request Body에는 DTO를 사용하며 `@Valid`로 검증.
- **의존성 주입**: Lombok의 `@RequiredArgsConstructor`를 통한 생성자 주입 사용.

### Service (`...Service.java` & `...ServiceImpl.java`)

- 인터페이스를 먼저 정의하고, 구현체(`Impl`)를 작성하는 구조.
- 비즈니스 로직은 이곳에 위치.
- 기본적으로 `@Transactional(readOnly = true)`를 사용하고, 쓰기 작업(CUD) 메서드에만 `@Transactional` 적용.

### Repository (`...Repository.java`)

- 기본적으로 `JpaRepository`를 상속.
- 복잡한 쿼리나 동적 쿼리는 `Custom` 인터페이스와 `Impl` 구현체를 만들고 QueryDSL을 사용.

### Entity (`...Entity.java`)

- **Lombok 사용**: `@Getter`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)`, `@AllArgsConstructor`, `@Builder` 권장.
- **상속**: `BaseEntity`를 상속받아 감사 필드(`created_at`, `updated_at`) 자동 관리.
- **Setter 금지**: 무분별한 `@Setter` 사용을 지양하고, 상태 변경이 필요하면 명확한 비즈니스 메서드(예: `updatePassword`)를 추가.

### DTO

- Java `record` 또는 `@Getter`가 있는 일반 클래스 사용.
- 요청(Request) DTO는 Jakarta Validation 어노테이션을 사용하여 유효성 검사 규칙 명시.

## 4. 예외 처리 (Exception Handling)

- `RuntimeException`을 상속받은 커스텀 예외 클래스 사용 (예: `InvalidTokenException`).
- `ControllerAdvice`에서 전역적으로 예외를 처리.
- 클라이언트에게는 표준화된 `ErrorResponse`와 적절한 HTTP 상태 코드를 반환.

## 5. 보안 및 개인정보 (Security & Privacy)

- **절대 금지**: 코드나 로그에 비밀번호, API 키, 인증 토큰, 개인정보(PII)를 하드코딩하거나 노출하지 말 것.
- **인증**: 현재 사용자 정보가 필요할 땐 컨트롤러 메서드 인자에서 `@AuthenticationPrincipal` 사용.

## 6. 테스트 (Testing)

- Service 레이어에 대한 단위 테스트 권장.
- Controller/Repository에 대해서는 `@SpringBootTest` 또는 `@DataJpaTest`를 활용한 통합 테스트 권장.
