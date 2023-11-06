# FineAnts

## 배포 주소

- [fineants](https://release.d3qzqon2e82ro5.amplifyapp.com/)

## 팀원 소개

|                                                         프론트엔드                                                          |                                                          프론트엔드                                                          |                                                           프론트엔드                                                           |                                                                       백엔드                                                                       |
|:----------------------------------------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------------------------------:|:-------------------------------------------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------------------------------------------------------:|
| <a href="https://github.com/bakhacode"><img src = "https://avatars.githubusercontent.com/u/114852081?v=4" width="120px;"> | <a href="https://github.com/Kakamotobi"><img src = "https://avatars.githubusercontent.com/u/79886384?v=4" width="120px;"> | <a href="https://github.com/altmit"><img src = "https://avatars.githubusercontent.com/u/41321198?v=4" width="120px;"> | <a href="https://github.com/yonghwankim-dev?tab=repositories"><img src = "https://avatars.githubusercontent.com/u/33227831?v=4" width="120px;"> |                                         |                                         |
|                                          [**박하**](https://github.com/bakhacode)                                          |                                         [**카카모토비**](https://github.com/Kakamotobi)                                         |                                         [**Jay**](https://github.com/altmit)                                          |                                         [**네모네모**](https://github.com/yonghwankim-dev?tab=repositories)                                         |

## 브랜치 협업 전략

- `release` : 릴리즈 배포 서버 브랜치
- `dev` : 개발 배포 서버 브랜치
- `dev-fe` : 프론트 엔드 개발 브랜치
  - fe/{브랜치타입}/#{이슈번호}-{목적}}
  - Ex: `fe/feat/#1-login`
- `dev-be` : 백엔드 개발 브랜치
- 일반 브랜치
  - {브랜치타입}/#{이슈번호}-{목적}
  - ex) feat/#1-login
  - ex) docs/#2-readme

## 템플릿

- 템플릿의 내용을 무조건적으로 지킬 필요 없이 필요한 경우 자유롭게 사용합니다.

### 기능 이슈 템플릿

```
제목 : [feat] 이슈 제목

## 구현해야 할것
- 내용

## 기타
- 내용
```

### 버그 이슈 템플릿

```
제목 : [bug] 이슈 제목

## 상황
- 내용

## 원인
- 내용

## 해결 방법
```

### PR 템플릿

```
제목 : [feat] PR 제목

## 구현한 것
- 내용

## 기타
- 내용
```

### 커밋 템플릿

```
#{이슈 번호} {커밋 타입}: {커밋 제목}

- {커밋 본문} (선택 사항)

ex.
#1 feat: add new feature

- 새로운 기능을 추가하였습니다.
```

### 커밋 타입

`feat` : 새로운 기능 추가

`fix` : 버그 수정

`refactor` : 코드 리팩토링

`test` : 테스트 코드

`docs` : 문서 수정

`style` : 코드 포맷팅, 세미콜론 누락, 코드 변경이 없는 경우

`design` : CSS 등 사용자 UI 디자인 변경

`chore` : 빌드 업무 수정, 패키지 매니저 수정 → 패키지 설치, 개발 환경 세팅

`merge` : merge

`rename` : 디렉토리 및 파일명 변경

`comment` : 주석 추가 혹은 오타 수정

`add` : 의존성 추가

## 코딩 컨벤션

### 프론트엔드

- 추가예정

### 백엔드

- 네이버 코딩 컨벤션 사용
- 체크 스타일 사용

## 그라운드 룰

- Slack
  - Slack 봤으면 👀달기
  - 수정/요청사항 있으면 Slack에 남기기
  - 관련 코멘트는 해당 메시지에 Slack 쓰레드로 남기기
- Communication Rules
  - 대화가 효율적이게 생각을 잘 정리해서 말하기
  - 자신의 의견을 서스럼없이 솔직하게 말하기
  - 다른 팀원의 의견에 귀 기울여 듣기

### 데일리 스크럼 룰

- 시간 : 10:00 ~ 10:30
- **스크럼** **내용:** 컨디션(10점 만점) / 어제 한 일 / 오늘 할 일 / 전체 공유

## 관련 링크

- [기획 및 디자인](https://www.figma.com/file/5hirTzTfhhaEl0K8B3RvIj/FineAnts?type=design&node-id=0-1&mode=design&t=uNazI9abfMLNWF09-0)
- [API 명세서](https://documenter.getpostman.com/view/30547529/2s9YR9YsEg)
- [BE WBS](https://github.com/orgs/fine-ants/projects/1/views/2)
- [스크럼](https://github.com/fine-ants/obsidian/tree/main/%EC%8A%A4%ED%81%AC%EB%9F%BC)

## ERD

<img width="811" alt="erd" src="https://github.com/fine-ants/FineAnts/assets/33227831/312b0e7a-6afc-4ccc-a89c-1934ef74bda1">

## 아키텍처
<img width="810" alt="fineAnts_architecture_v2" src="https://github.com/fine-ants/FineAnts/assets/33227831/a6e48d4c-6afa-49a3-9188-e311ae899a3f">

## 데모 영상

- 추가 예정
