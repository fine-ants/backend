# :pushpin: FineAnts

> 주식 포트폴리오 가상 관리 및 알림 서비스  
> https://www.fineants.co/

## 1. 제작 기간 & 참여 인원

- 2023 10월 10일 ~ 진행중
- 팀 프로젝트
    - 백엔드
        - [네모네모](https://github.com/yonghwankim-dev)
        - [이예인](https://github.com/yein-lee)
        - [무비](https://github.com/yhpark95)
    - 프론트엔드
        - [박하](https://github.com/bakhacode)
        - [Jay](https://github.com/altmit)
        - [카카모토비](https://github.com/Kakamotobi)

## 2. 사용 기술

- Java 11
- Spring Boot 2.7
- Gradle
- Spring Data JPA
- QueryDSL
- Firebase-admin
- Spring AWS
- MySQL 8.0
- Redis

## 3. ERD 설계

![image](https://github.com/fine-ants/FineAnts-was/assets/33227831/f48d2492-844e-4066-9a09-c34194fb6a28)

## 4. 핵심 기능

- 포트폴리오 관리
    - 포트폴리오 생성
    - 포트폴리오 종목 추가
    - 포트폴리오 매입 이력 추가
- 포트폴리오 상세 정보 실시간 조회
- 포트폴리오 목표 수익률/최대 손실율 알림
- 종목의 현재가 및 종가 조회

## 5. 핵심 트러블 슈팅

- 알림 관련 문제
    - [x] 매입 이력 추가 후 목표수익률 이벤트 지연 로딩 문제
    - [x] FCM 토큰 등록 오류
    - [x] 회원 알림 관련 API 권한 문제
- 한국투자증권 관련 문제
    - [x] 한국투자증권 액세스 토큰 만료시 발급 문제
    - [x] redis accessTokenMap 만료시간 문제
    - [x] 종목 종가 갱신 문제
    - [x] 서버 시작시 KIS 액세스 토큰 발급 실패 문제
    - [x] 종가 갱신 스케줄링 메소드 실행전 액세스 토큰 발급 문제
- SSE
    - [x] Hikari Connection Pool 고갈 문제
    - [x] 포트폴리오 상세 조회 SSE 데이터 응답 문제
- API
    - [x] 종목 최신화시 데이터가 db에 반영되지 않는 문제

## 6. 그 외 트러블 슈팅

<details>
<summary>redis 컨테이너 실행시 rdb 파일을 생성하지 않도록 설정</summary>
<div markdown="1">

- Redis의 스냅샷 작성시 실패하게 되면 Write 명령어를 전부 거부함에 따라 rdb(redis database) 파일을 생성하지 않도록 설정

```
stop-writes-on-bgsave-error no
save ""
```

- [issue#38](https://github.com/fine-ants/FineAnts-was/issues/38)

</div>
</details>

<details>
<summary>종목 지정가 도달 알림 문제 해결</summary>
<div markdown="1">

- 종목 지정가 알림 전송 이력 전송시 알림마다 생성되는 등록번호(PK, Notification.id)를 키값으로 저장하는 것이 아닌
  종목 지정가 데이터에 대한 등록번호(PK, TargetPriceNotification.id)를 기준으로 저장합니다.

```
// 발송 이력 저장
.map(future -> future.thenCompose(item -> {
	sentManager.addTargetPriceNotification(item.getTargetPriceNotificationId());
	return CompletableFuture.supplyAsync(() -> item);
}))
```

- [issue#268](https://github.com/fine-ants/FineAnts-was/issues/268)

</div>
</details>

<details>
<summary>프로필 변경 문제</summary>
<div markdown="1">

- 프로필 정보에서 프로필 이미지만 변경하는 경우에 텍스트 정보가 필수값으로 설정되어 있어서 옵션을 선택적으로 변경

```
public ApiResponse<ProfileChangeResponse> changeProfile(
		@RequestPart(value = "profileImageFile", required = false) MultipartFile profileImageFile,
		@Valid @RequestPart(value = "profileInformation", required = false) ProfileChangeRequest request,
		@AuthPrincipalMember AuthMember authMember)
```

- [issue#164](https://github.com/fine-ants/FineAnts-was/issues/164)

</div>
</details>

<details>
<summary>비밀번호 변경 문제</summary>
<div markdown="1">

- 비밀번호 변경 서비스 메서드에서 `@Transactional(readOnly=true)`를 `@Transactional`로 변경하여 해결

```
@Transactional
public void modifyPassword(ModifyPasswordRequest request, AuthMember authMember) {
```

- [issue#162](https://github.com/fine-ants/FineAnts-was/issues/162)

</div>
</details>

<details>
<summary>회원가입 서비스 문제</summary>
<div markdown="1">

- 회원가입시 프로필 사진과 json 형식의 회원가입 정보를 같이 전달하는 경우 json 데이터가 전달되지 않는 문제
- 클라이언트인 React에서 로컬 개발시 목서버를 끔으로써 문제를 해결
- [issue#159](https://github.com/fine-ants/FineAnts-was/issues/159)

</div>
</details>

<details>
<summary>회원 닉네임 랜덤 생성 길이 10자로 제한</summary>
<div markdown="1">

- member.nickname.len 프로퍼티의 길이를 7로 설정하여 문제를 해결
- 랜덤 닉네임 형식 : 일개미(3자) + 랜덤 문자열 7자

```yml
member:
  nickname:
    prefix: 일개미
    len: 7
```

- [issue#154](https://github.com/fine-ants/FineAnts-was/issues/154)

</div>
</details>

<details>
<summary>일반 로그인 오타 수정</summary>
<div markdown="1">

- 로컬 회원을 db에서 조회시 provider(플랫폼) 매개변수에 null이 아닌 "local"을 전달하여 문제를 해결

```
@Transactional(readOnly = true)
public LoginResponse login(LoginRequest request) {
  Member member = memberRepository.findMemberByEmailAndProvider(request.getEmail(), LOCAL_PROVIDER)
  .orElseThrow(() -> new BadRequestException(MemberErrorCode.LOGIN_FAIL));
  // ...
  return LoginResponse.from(jwt, OauthMemberResponse.from(member));
}
```

- [issue#133](https://github.com/fine-ants/FineAnts-was/issues/133)

</div>
</details>

<details>
<summary>라인 차트 조회시 데이터 정렬</summary>
<div markdown="1">

- 포트폴리오들의 전체 평가금액에 대한 라인 차트 조회시 일자를 기준으로 오름차순으로 정렬하여 문제를 해결

```
return timeValueMap.keySet()
		.stream()
		.sorted()
		.map(key -> DashboardLineChartResponse.of(key, timeValueMap.get(key)))
		.collect(Collectors.toList());
```

- [issue#84](https://github.com/fine-ants/FineAnts-was/issues/84)

</div>
</details>

<details>
<summary>포트폴리오 단일 삭제 문제</summary>
<div markdown="1">

- 포트폴리오 단일 삭제시 일대다 관계를 맺고 있는 포트폴리오의 수익 내역 데이터(PortfolioGainHistory)들을 먼저 제거하여 단일 삭제 문제를 해결

```
int delPortfolioGainHistoryCnt = portfolioGainHistoryRepository.deleteAllByPortfolioId(portfolioId);
log.info("포트폴리오 손익 내역 삭제 개수 : {}", delPortfolioGainHistoryCnt);
```

- [issue#83](https://github.com/fine-ants/FineAnts-was/issues/83)

</div>
</details>

<details>
<summary>AuthMember 문제</summary>
<div markdown="1">

- AuthMember 타입에 대한 매개변수 리졸버를 설정 클래스 파일에 추가함으로써 문제를 해결

```java

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
	private final AuthPrincipalArgumentResolver authPrincipalArgumentResolver;

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(authPrincipalArgumentResolver);
	}
}
```

- [issue#31](https://github.com/fine-ants/FineAnts-was/issues/31)

</div>
</details>

<details>
<summary>로그아웃 문제</summary>
<div markdown="1">

- 로그아웃이 정상적으로 수행하기 위해서 로그아웃 인터셉터를 설정 클래스 파일에 추가하여 문제를 해결

```java

@Slf4j
public class LogoutInterceptor implements HandlerInterceptor {
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws
		Exception {
		log.debug("로그아웃 인터셉터 접속 : {}", request.getRequestURI());
		String accessToken = extractJwt(request).orElseThrow(
			() -> new UnAuthorizationException(JwtErrorCode.EMPTY_TOKEN));
		request.setAttribute("accessToken", accessToken);
		return true;
	}

	private Optional<String> extractJwt(HttpServletRequest request) {
		String header = request.getHeader(AUTHORIZATION);

		if (!StringUtils.hasText(header) || !header.startsWith(BEARER)) {
			return Optional.empty();
		}

		return Optional.of(header.split(" ")[1]);
	}
}
```

- [issue#29](https://github.com/fine-ants/FineAnts-was/issues/29)

</div>
</details>

<details>
<summary>sse 전송 시간 문제</summary>
<div markdown="1">

- 다수의 클라이언트가 동일한 포트폴리오 번호를 이용하여 실시간 포트폴리오 상세 정보(sse 방식) 요청시 한 클라이언트를 제외한 다른 클라이언트의 연결이 끊어지는 문제
- sseEmitter 객체의 관리를 해시맵으로 관리하고 있었고 해시맵의 키값을 포트폴리오 번호로 관리하였기 때문에 발생한 문제
- sseEmitter 관리하는 해시맵의 키값을 이벤트 ID와 포트폴리오 등록번호를 가진 SseEmitterKey 타입으로 변경하여 문제를 해결

```java

@Getter
@ToString
@EqualsAndHashCode(of = "eventId")
@RequiredArgsConstructor
public class SseEmitterKey {
	private final Long eventId;
	private final Long portfolioId;

	public static SseEmitterKey create(Long portfolioId) {
		return new SseEmitterKey(
			System.currentTimeMillis(),
			portfolioId
		);
	}
}
```

- [issue#140](https://github.com/fine-ants/FineAnts-was/pull/140)

</div>
</details>

## 7. 회고 / 느낀점

> 프로젝트 개발 회고 글: https://yonghwankim-dev.tistory.com/599
