# TCP Chat Scale-up Roadmap

텍스트 채팅을 TCP/TLS(WebSocket 포함) 기반으로 단계별로 확장하며, 성능을 측정/기록하는 것이 목표입니다. WebRTC·미디어는 범위 밖입니다.

## 현재 레벨 해석 (Lv1~Lv3)

- `Lv1_TCP_Connection`: 단일 클라이언트와의 TCP 핸드셰이크·문자열 왕복. JUnit으로 에코 검증.
- `Lv2_TCP_Chat`: 스레드 per client, 단일 룸 브로드캐스트. 닉네임 식별, `exit` 처리.
- `Lv3_TCP_MultiRoomChat`: 멀티 룸 설계(룸 매니저/룸 객체). 클라이언트는 입장까지 구현, 입장 후 대화 루프 미구현.

## 앞으로의 레벨 재정의(채팅 스케일업)

- **Lv3 보완**: 멀티룸 클라이언트 대화 루프/exit 완성, 방 비우기 시 삭제 정책, 예외/정리 로깅.
- **Lv4: Netty/NIO 전환**
  - 이벤트 기반 서버, 길이 프레이밍, 메시지 사이즈 제한, 스레드풀/백프레셔 정책.
  - 목표: 단일 노드 300~500 TPS(브로드캐스트 포함).
- **Lv5: 신뢰성/순서 보존**
  - 메시지 ID·룸별 seq, ACK/재전송, 중복 제거, 클라이언트 재접속 시 미수신 재동기화(Last-Read 기반 fetch).
  - 목표: 800~1200 TPS.
- **Lv6: 분산 팬아웃**
  - Pub/Sub(Kafka/Redis Streams 등) 도입, 게이트웨이/컨슈머 분리, 대규모 룸 팬아웃(샤딩 or pull).
  - 목표: 2k~5k TPS(환경 따라 조정).
- **Lv7: 운영/폭주 제어**
  - 레이트 리밋, 큐 드롭 정책, 멀티 리전 라우팅, 프레즌스 확장, 알람/옵저버빌리티 고도화.

## 통합 부하 도구 방향(레벨 공용)

- 단일 바이너리 + 프로토콜 어댑터(`tcp`, `ws`, `future: netty`, `future: kafka-gw`). 공통 CLI/메트릭 유지.
- 공통 시나리오 포맷(필수 키만): `scenario`, `protocol`, `host`, `port`, `clients`, `ramp`, `duration`, `msg_size`, `msg_interval`, `room_mode(single|multi)`, `room_name_or_count`. 나머지는 기본값 사용.
- 구성 요소: `ProtocolClient`(connect/join/send/exit), `ScenarioRunner`, `WorkerPool`(램프업), `MetricsCollector`(TPS, p50/p95, 실패율, disconnects), `Reporter`(콘솔+CSV/JSON).

## 표준 부하 시나리오(모든 레벨에 공통 적용)

- **smoke**: 5명, 5s, 1s 간격, 기본 기능/정상 종료 확인.
- **single-room**: 30명, 60s, 1s 간격, 단일 룸 브로드캐스트 기본 지표.
- **multi-room**: 5개 룸 × 룸당 10명, 120s, 500ms 간격. 룸 분산 브로드캐스트.
- **large-room**: 1개 룸 50명 이상, 120s, 500ms 간격. 팬아웃 병목 확인.
- **reconnect**: 30명, 120s, 10s마다 disconnect/reconnect, 실패율/복구 측정.
- **backpressure**: 메시지 간격 50~100ms, 메시지 길이 상한 근접. 서버의 큐/스레드풀 거동 확인.

## 실행 예시(통합 CLI 가정)

```
java -jar loadgen.jar \
  --scenario single-room \
  --protocol tcp \
  --host 127.0.0.1 --port 5555 \
  --clients 50 --ramp 10/s \
  --duration 60s --msg-size 64 --room main
```

## TPS 목표/실측 기록표

| 레벨 | 환경/기능                  | 목표 TPS | 실측 TPS | p50/p95(ms) | 실패율 | 비고           |
| ---- | -------------------------- | -------- | -------- | ----------- | ------ | -------------- |
| Lv1  | 단일 세션 에코             | 5~10     | (미측정) | -           | -      | 로컬 단일 연결 |
| Lv2  | 스레드 per client 단일 룸  | 50~100   | (미측정) | -           | -      | 20~30 동접     |
| Lv3  | 멀티 룸, 스레드 per client | 100~200  | (미측정) | -           | -      | 룸당 10명      |
| Lv4  | Netty/NIO, 프레이밍        | 300~500  | (미측정) | -           | -      | 이벤트 기반    |
| Lv5  | ACK/재전송/seq             | 800~1200 | (미측정) | -           | -      | 재동기화 포함  |
| Lv6  | Pub/Sub 팬아웃             | 2k~5k    | (미측정) | -           | -      | 샤딩/대규모 룸 |
| Lv7  | 폭주 제어/멀티 리전        | TBD      | (미측정) | -           | -      | 운영 지표 중심 |

## 측정/기록 방법

1. 표준 시나리오 중 최소 `single-room`, `multi-room`, `large-room` 3개를 각 레벨 전환 시마다 실행.
2. `Reporter` 출력(TPS, p50/p95, 실패율, disconnects)을 표에 옮기고, 병목과 주요 튜닝 포인트를 README에 짧게 주석으로 남김.
3. 목표 미달 시 원인(GC, 스레드풀, 락, 네트워크)과 조치안을 별도 bullet로 기록.
