# Java 인증서 차단 정책

CVE-2026-91403 대응을 위해 OpenJDK가 안내한 Unix용 공식 `blocked.certs`를 보관한다.
Dockerfile은 의존성을 내려받는 JDK와 애플리케이션을 실행하는 JRE 모두에 이 파일을
병합한다. 베이스 이미지에 있는 기존 차단 항목을 보존하므로 새 베이스 이미지의
추가 보안 정책도 유지된다. 원래의 CA 신뢰 저장소와 인증서 검증 옵션은 바꾸지 않는다.

- 원본: https://ops.java/unix/blocked.certs
- 확인일: 2026-10-22
- 원본 SHA-256: `2596bb6dda83c66d96c5a252d7b2ae1f24cc1ae3f1890223b4f3591a8d52bf10`
- 원본 내용: `Algorithm=SHA-256`과 차단 fingerprint 86개
- OpenJDK 권고: https://openjdk.org/groups/vulnerability/advisories/2026-10-21
- Oracle 운영 안내: https://ops.java/emudhra

공식 파일을 갱신할 때는 출처와 해시를 확인하고, 기존 차단 항목이 유지되는지 검사한다.
재빌드한 Linux ARM64 이미지에서 공개된 침해 인증서 테스트 체인이 거부되는지와
필요한 정상 TLS 연결이 유지되는지를 모두 검증한다. 네트워크 오류는 차단 성공으로
집계하지 않는다. 이미지 또는 베이스 JDK를 바꾼 뒤에도 이 검증을 반복한다.

이 정책은 공개된 침해 인증서에 대한 공식 대응이다. Java의 다른 보안 업데이트를
대체하지 않으며, 특정 JDK 버전 번호만으로 이 CVE의 해결을 판정하지 않는다.
