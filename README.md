# 임베딩 검색 프로그램

> 시작: [코사인 유사도와 벡터 검색](https://jinsanpark.github.io/posts/Embedding-Project/)

뉴스 기사를 임베딩해서 의미로 검색하는 웹 애플리케이션.
"코스피 하락"으로 검색하면 그 단어가 없는 "사이드카 이틀 연속 발동" 같은 기사도 찾아줌.
학습 목적의 개인 프로젝트로, 현재 기사 데이터는 저작권 문제를 피하기 위해 더미 데이터를 사용.

Spring Boot / PostgreSQL + pgvector / Voyage AI 임베딩.

## 성능 개선 요약

300건 기준, 검색 한 번의 응답 시간.

| 단계 | 응답 시간 | 커밋 |
|---|---|---|
| 최초 — 매 검색마다 전건 조회 + 파싱 | 225.3 ms | [`c64e9bd`](https://github.com/JinsanPark/NewsFinder/commit/c64e9bd2b9e0b7a8c2a83118a8d1b9617c3e4828) |
| pgvector로 계산 이관 | 196.8 ms | [`2ae30ab`](https://github.com/JinsanPark/NewsFinder/commit/2ae30abc6c47732c82fbca1910143c5b033691d5) |
| 검색어 벡터 캐시 (히트) | 28.2 ms | [`8c1cc41`](https://github.com/JinsanPark/NewsFinder/commit/8c1cc41959e1aa1527e9967bf1a4c31e98f59392) |

적재는 배치 요청으로 58.6초 -> 3.5초.

이후 메모리 캐시를 한 겹 더 얹어 3층으로 만듦(`c622712`). DB 왕복 15.2 ms를 추가로 줄임.
서버 내부 구간만 잰 값이라 위 표와 경계가 달라 같은 줄에 넣지 않음.

⚠️ 위 세 값은 **측정 시점이 서로 다름.** 응답의 대부분을 차지하는 Voyage API 왕복이
측정할 때마다 183~197 ms 사이로 흔들리므로, 단계 간 차이를 그대로 개선폭으로 읽으면 안 됨.

측정 방법, 조건, 원자료는 [docs/performance.md](docs/performance.md).

## 실행

환경변수: `VOYAGE_API_KEY`, `DB_HOST`, `DB_USERNAME`, `DB_PASSWORD`

1. postgres 실행 — `pgvector/pgvector` 이미지 사용, DB 생성 후 `CREATE EXTENSION vector;`
2. 아래 인덱스 생성
3. `init` 프로파일로 실행하면 `data/news_articles_dummy.json`을 적재 (최초 1회)
4. 프로파일 없이 실행하면 검색 서버

```sql
CREATE INDEX news_embedding_hnsw ON news USING hnsw (embedding vector_cosine_ops);
```

이 인덱스는 코드로 관리되지 않으므로 새 환경에서는 직접 실행 필요.

## 무엇을 개선했나

### 적재 — 배치 요청

> 과정: [임베딩 그리고 배치](https://jinsanpark.github.io/posts/Embedding-Project-2/)

300건 적재에 58초가 소요되며, 그중 대부분이 건당 약 200 ms의 API 응답 대기.
`input` 파라미터가 배열을 받는 점을 이용해 128건씩 묶어 호출하도록 변경. 300회 -> 3회.

### 검색 — 계산을 DB로

> 과정: [캐시로 성능 향상 시키기?](https://jinsanpark.github.io/posts/Embedding-Project-3/) · [pgvector](https://jinsanpark.github.io/posts/Embedding-Project-4/) · [검색 성능 측정](https://jinsanpark.github.io/posts/Embedding-Project-5/)

유사도 계산을 자바에서 pgvector로 옮김. 19,200건에서 2,136.8 ms -> 287.4 ms.

다만 **300건 규모에서는 계산 방식을 바꿔도 얻는 것이 거의 없음.** 응답의 87%가 Voyage API 왕복이고
DB 쿼리 실행은 1%에 불과하기 때문. 같은 이유로 이 규모에서는 플래너가 HNSW 인덱스를 선택하지 않음.

-> [측정 상세](docs/performance.md#검색-성능)

### 검색어 벡터 캐시

> 과정: [벡터 재활용](https://jinsanpark.github.io/posts/Embedding-Project-6/)

응답의 87%가 검색어 임베딩 API 대기였으므로, 한 번 만든 검색어 벡터를 저장해 재사용.
`(정규화된 검색어, 모델명)`을 키로 조회하고, 있으면 API를 호출하지 않음.

캐시 히트 시 226.4 ms -> 28.2 ms. 미스는 조회·저장이 붙어 도입 전보다 느려짐.

이후 DB 캐시 앞에 메모리 캐시(LRU, 용량 100)를 한 겹 더 얹음.

```
L1(메모리) -> DB(query_vector_cache) -> Voyage API
```

경로별로 0.0098 ms / 15.2 ms / 216.5 ms. **L1이 줄인 것은 DB 왕복 15.2 ms**로,
DB 캐시가 걷어낸 약 204 ms의 7% 수준임. 손익분기 히트율은 14.6%.

-> [측정 상세](docs/performance.md#검색어-벡터-캐시) · [L1 메모리 캐시](docs/performance.md#l1-메모리-캐시)

## 기록

- 측정 절차와 집계 규칙: [data/measurement-protocol.md](data/measurement-protocol.md)
- 원자료: [data/raw/](data/raw/)
- 과정 기록: [블로그 시리즈](https://jinsanpark.github.io/categories/newsfinder/)

2026-08-28 측정값 일부는 2026-09-03 재측정에서 변화가 있었음.
[정정 내역](docs/performance.md#2026-08-28-측정-정정) 참고.
