# gRPC 기반 체크아웃 서비스 흐름

---

## 전체 흐름 요약

```
클라이언트
  → (REST) Checkout Service
      → (gRPC) User Service       : 유저 검증
      → (gRPC) Product Service    : 재고 확인 (병렬)
      → (gRPC) Coupon Service     : 쿠폰 검증 (병렬)
      → (gRPC) Payment Service    : 결제 처리
      → (gRPC) Product Service    : 재고 차감
```

---

## 1. Checkout Service (오케스트레이터)

클라이언트의 REST 요청을 받아 각 서비스를 순서에 맞게 호출하는 진입점.

**흐름:**

1. 클라이언트로부터 `userId`, `items`, `couponCode`, `deliveryAddress` 수신
2. User Service에 유저 유효성 검증 요청
3. Product Service와 Coupon Service에 병렬로 재고 확인 및 쿠폰 검증 요청 (코루틴 활용)
4. 최종 결제 금액 계산 (상품 총액 - 쿠폰 할인)
5. Payment Service에 결제 처리 요청
6. 결제 성공 시 Product Service에 재고 차감 요청
7. 클라이언트에 `orderId`, `finalAmount`, `status` 응답

**실패 처리:**

- 유저 검증 실패 → 즉시 요청 거부
- 재고 부족 → 결제 시도 없이 거부
- 결제 실패 → 재고 차감 없이 종료

---

## 2. User Service

유저의 유효성을 검증하는 서비스.

**흐름:**

1. Checkout Service로부터 `userId`, `deliveryAddress` 수신
2. DB에서 유저 존재 여부 확인
3. 유저 상태 확인 (탈퇴/정지 여부)
4. 배송지 유효성 확인
5. 검증 결과 (`isValid`, `userName`) 응답

**실패 케이스:**

- 존재하지 않는 유저 → `NOT_FOUND` 에러
- 정지된 유저 → `PERMISSION_DENIED` 에러

---

## 3. Product Service

상품 재고 확인 및 차감을 담당하는 서비스.

**흐름 (재고 확인):**

1. Checkout Service로부터 `items` (상품 ID + 수량 목록) 수신
2. 각 상품의 재고 조회
3. 요청 수량과 재고 비교
4. 상품별 가격 계산 및 총액 산출
5. 재고 가용 여부 및 `totalPrice` 응답

**흐름 (재고 차감):**

1. 결제 완료 후 Checkout Service로부터 차감 요청 수신
2. 각 상품 재고 차감 (동시성 처리 필요 → 낙관적 락 or 분산 락)
3. 차감 성공 여부 응답

**실패 케이스:**

- 재고 부족 → `FAILED_PRECONDITION` 에러
- 차감 중 동시성 충돌 → 재시도 or 실패 응답

---

## 4. Coupon Service

쿠폰 유효성 검증 및 할인 금액 계산을 담당하는 서비스.

**흐름:**

1. Checkout Service로부터 `couponCode`, `userId`, `totalPrice` 수신
2. 쿠폰 존재 여부 확인
3. 쿠폰 만료 여부 확인
4. 해당 유저의 쿠폰 사용 이력 확인 (중복 사용 방지)
5. 할인 금액 계산 (정액 or 정률)
6. `discountAmount`, `finalPrice` 응답

**쿠폰 코드가 없는 경우:**

- 빈 값으로 요청 시 `discountAmount = 0` 응답 (에러 아님)

**실패 케이스:**

- 존재하지 않는 쿠폰 → `NOT_FOUND` 에러
- 만료된 쿠폰 → `FAILED_PRECONDITION` 에러
- 이미 사용한 쿠폰 → `ALREADY_EXISTS` 에러

---

## 5. Payment Service

실제 결제 처리를 담당하는 서비스.

**흐름:**

1. Checkout Service로부터 `userId`, `amount`, `orderId` 수신
2. 결제 수단 확인 (등록된 카드 등)
3. 결제 처리 (외부 PG사 연동 or 시뮬레이션)
4. 결제 내역 저장
5. `paymentId`, `status`, `paidAt` 응답

**실패 케이스:**

- 잔액 부족 or 카드 오류 → `FAILED_PRECONDITION` 에러
- PG사 타임아웃 → `DEADLINE_EXCEEDED` 에러 (재시도 로직 필요)

---

## gRPC 에러 코드 정리

|상황|gRPC Status Code|
|---|---|
|리소스 없음 (유저, 쿠폰 등)|`NOT_FOUND`|
|권한 없음 (정지 유저 등)|`PERMISSION_DENIED`|
|조건 불충족 (재고 부족, 쿠폰 만료 등)|`FAILED_PRECONDITION`|
|중복 (이미 사용한 쿠폰 등)|`ALREADY_EXISTS`|
|타임아웃|`DEADLINE_EXCEEDED`|
|서버 내부 오류|`INTERNAL`|

---

## 구현 시 핵심 포인트

- **병렬 호출**: Product Service와 Coupon Service는 의존성이 없으므로 코루틴으로 동시 호출
- **gRPC 인터셉터**: JWT 인증, 로깅, 에러 핸들링을 인터셉터로 공통 처리
- **타임아웃**: 각 서비스 호출에 deadline 설정 (Payment Service는 넉넉하게)
- **멱등성**: 결제 요청은 동일한 `orderId`로 중복 요청 시 같은 결과 반환