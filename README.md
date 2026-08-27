# 임베딩 검색 프로그램

> 시작: [코사인 유사도와 벡터 검색](https://jinsanpark.github.io/posts/Embedding-Project/)

뉴스 기사를 임베딩해서 의미로 검색하는 웹 애플리케이션.
"코스피 하락"으로 검색하면 그 단어가 없는 "사이드카 이틀 연속 발동" 같은 기사도 찾아준다.
학습 목적의 개인 프로젝트로, 기사 데이터는 저작권 문제를 피하기 위해 더미 데이터를 사용.

Spring Boot / PostgreSQL + pgvector / Voyage AI 임베딩.

## 성능 개선 요약

300건 기준, 검색 한 번의 응답 시간.

| 단계 | 응답 시간 | 커밋 |
|---|---|---|
| 최초 — 매 검색마다 전건 조회 + 자바 계산 | 225.3 ms | [`c64e9bd`](https://github.com/JinsanPark/NewsFinder/commit/c64e9bd2b9e0b7a8c2a83118a8d1b9617c3e4828) |
| pgvector로 계산 이관 | 196.8 ms | [`95d49ec`](https://github.com/JinsanPark/NewsFinder/commit/95d49ecefc6cbe7e71825ced8ea09e0ba92fca82) |
| 검색어 벡터 캐시 (히트) | 28.2 ms | [`8c1cc41`](https://github.com/JinsanPark/NewsFinder/commit/8c1cc41959e1aa1527e9967bf1a4c31e98f59392) |

적재는 배치 요청으로 58.6초 → 3.5초.

각 단계의 측정 근거와 조건은 아래에.

## 실행

환경변수: `VOYAGE_API_KEY`, `DB_HOST`, `DB_USERNAME`, `DB_PASSWORD`

1. postgres 실행 — `pgvector/pgvector` 이미지 사용, DB 생성 후 `CREATE EXTENSION vector;`
2. 아래 인덱스 생성
3. `init` 프로파일로 실행하면 `data/news_articles_dummy.json`을 적재 (최초 1회)
4. 프로파일 없이 실행하면 검색 서버

```sql
CREATE INDEX news_embedding_hnsw ON news USING hnsw (embedding vector_cosine_ops);
```

이 인덱스는 코드로 관리되지 않으므로 새 환경에서는 직접 실행해야 함.

## 적재 성능 - 배치 요청

> 과정: [임베딩 그리고 배치](https://jinsanpark.github.io/posts/Embedding-Project-2/)

300건 적재 시 58초 소요. 요청 1건당 약 200ms가 Voyage API 응답 대기 시간이었음. (기사 제목 + 요약 임베딩 기준)
`input` 파라미터가 배열을 받는 점을 이용해 128건씩 묶어 호출하도록 변경.

| | 요청 횟수 | 소요 시간 |
|---|---|---|
| 개선 전 | 300회 | 58.6초 |
| 개선 후 | 3회 | 3.5초 |

동일 데이터 기준 1회 측정. 네트워크 상황에 따라 변동 가능.

## 검색 성능 - 캐시, pgvector, 인덱스

> 과정: [캐시로 성능 향상 시키기?](https://jinsanpark.github.io/posts/Embedding-Project-3/) · [pgvector, 계산을 DB한테 떠넘기기](https://jinsanpark.github.io/posts/Embedding-Project-4/) · [검색 성능 측정](https://jinsanpark.github.io/posts/Embedding-Project-5/)

더미 기사 300건을 복제해 2,400건, 19,200건으로 늘려가며 측정.

| 방식 | 커밋 | 300건 | 2,400건 | 19,200건 |
|---|---|---|---|---|
| 캐시 없음 (매 검색마다 전건 조회 + 파싱) | [`c64e9bd`](https://github.com/JinsanPark/NewsFinder/commit/c64e9bd2b9e0b7a8c2a83118a8d1b9617c3e4828) | 225.3 ms | 427.2 ms | 2,136.8 ms |
| 자바 메모리 캐시 | [`d234a6a`](https://github.com/JinsanPark/NewsFinder/commit/d234a6aeb6224f504666894c63bcd84d4d84bb67) | 196.7 ms | 239.8 ms | 525.2 ms |
| pgvector (인덱스 없음) | [`95d49ec`](https://github.com/JinsanPark/NewsFinder/commit/95d49ecefc6cbe7e71825ced8ea09e0ba92fca82) | 196.8 ms | 211.2 ms | 287.4 ms |
| pgvector + HNSW 인덱스 | [`56f0197`](https://github.com/JinsanPark/NewsFinder/commit/56f01973929baad49f4891b3c9d543790236156e) + 위 인덱스 | — | — | 209.2 ms |

인덱스 측정은 19,200건만 수행.
응답 시간에는 검색어를 벡터로 바꾸는 Voyage API 왕복이 포함되어 있음. (아래 참조)

### 검색 한 번의 구성 (캐시 도입 전)

300건, 인덱스 적용, 서버 내부 측정 500회의 중앙값.

| 구간 | 시간 |
|---|---|
| Voyage API 왕복 (검색어 임베딩) | 197.8 ms |
| DB 조회 + 결과 변환 | 30.5 ms |
| 합계 | 228.3 ms |

응답 시간의 87%가 외부 API 대기.
DB 구간에는 앱 <-> postgres 왕복, 1024차원 벡터 파라미터 전송, 쿼리 실행, 결과 매핑이 모두 포함됨.
별도로 `SELECT 1`을 실행한 왕복 기준선은 약 7ms (DBeaver 기준, 클라이언트가 달라 정밀 비교는 아님).

위 응답 시간 표와는 측정 시점·방법이 달라 직접 차감할 수 없음.

### 건당 비용

API 왕복은 건수와 무관한 고정 비용이므로, 두 지점의 차이로 산출.

```
(19,200건 값 − 2,400건 값) ÷ 16,800
```

| 방식 | 건당 |
|---|---|
| 캐시 없음 (조회 + 파싱 + 계산) | 101.8 μs |
| 자바 캐시 (계산만) | 17.0 μs |
| pgvector (DB 스캔) | 4.5 μs |

셋 다 전건 스캔이므로 O(n). 상수만 다름.

## 검색어 벡터 캐시

> 과정: [벡터 재활용](https://jinsanpark.github.io/posts/Embedding-Project-6/)

응답 시간의 87%가 검색어 임베딩 API 대기였으므로, 한 번 만든 검색어 벡터를 DB에 저장해 재사용.
`(정규화된 검색어, 모델명)`을 키로 조회하고, 캐시에 있으면 API를 호출하지 않음.
검색어는 `NFC → 앞뒤 공백 제거 → 연속 공백 축약 → 소문자` 순으로 정규화하며, 키와 API 입력에 같은 문자열을 사용.

모델명을 키에 포함시킨 이유는, 모델을 교체했을 때 이전 모델이 만든 벡터가 조용히 재사용되는 것을 막기 위함.
차원 수만 같으면 유사도 계산이 오류 없이 수행되므로 결과만 어긋나고 에러는 나지 않음.

| 구분 | 커밋 | 중앙값 | p95 |
|---|---|---|---|
| 캐시 도입 전 (pgvector + 인덱스, 300건) | [`95d49ec`](https://github.com/JinsanPark/NewsFinder/commit/95d49ecefc6cbe7e71825ced8ea09e0ba92fca82) | 196.8 ms | 233.8 ms |
| 캐시 히트 | [`8c1cc41`](https://github.com/JinsanPark/NewsFinder/commit/8c1cc41959e1aa1527e9967bf1a4c31e98f59392) | 28.2 ms | 44.8 ms |
| 캐시 미스 | [`8c1cc41`](https://github.com/JinsanPark/NewsFinder/commit/8c1cc41959e1aa1527e9967bf1a4c31e98f59392) | 286.0 ms | 323.3 ms |

히트 시 API 왕복이 통째로 빠져 약 7배 빨라짐.
미스는 캐시 조회와 저장이 추가되어 도입 전보다 오히려 느려짐.

캐시 동작 테스트: [`261491b`](https://github.com/JinsanPark/NewsFinder/commit/261491b7a554683483a3bf60863a9bae6b4e4447)

### 캐시 미스 한 번의 구성

서버 내부 측정 550회의 중앙값.

| 구간 | 시간 |
|---|---|
| 정규화 + 캐시 조회 | 12.1 ms |
| Voyage API 왕복 | 197.1 ms |
| 캐시 저장 | 77.7 ms |

측정 지점: `QueryVectorService.getVector()` 진입 / `embedQuery()` 전후 / `save()` 직후.

API 왕복 197.1 ms는 캐시 도입 전 측정값 197.8 ms와 사실상 동일.
즉 늘어난 약 90 ms는 캐시 조회와 저장 비용이며, API가 느려진 것이 아님.

구간별 중앙값의 합은 전체 중앙값과 일치하지 않음. 비중 파악용으로만 볼 것.

### 손익분기

유사도 검색은 히트·미스·캐시 없음 모두에 동일하게 포함되므로 계산에서 상쇄됨.

```
손익분기 히트율 = (캐시 저장 + 캐시 조회) ÷ (API 왕복 + 캐시 저장)
                = (77.7 + 12.1) ÷ (197.1 + 77.7)
                = 32.7%
```

실측값(28.2 / 286.0 / 196.8)으로 풀면 34.6%.
API 왕복과 캐시 저장은 건수와 무관한 고정 비용이므로, 데이터가 늘어도 이 비율은 거의 변하지 않음.

개선 여지: 캐시 저장 77.7 ms는 벡터를 반환한 뒤로 미룰 수 있음.
응답 경로에서 빼면 미스도 200 ms 초반으로 내려감.

<details>
<summary>측정 환경</summary>

- 애플리케이션: 데스크탑에서 실행 (Ryzen 7 7800X3D / 32GB / Windows 11)
- **캐시 없음 / 자바 캐시**: H2 (임베디드, 앱과 동일 프로세스), JVM 힙 기본값 (32GB 기준 최대 약 8GB)
- **pgvector / pgvector + 인덱스 / 검색어 벡터 캐시**: PostgreSQL 17.10 + pgvector 0.8.3.
  랩탑(Ryzen 7 5800H / 32GB / SSD)에서 도커로 실행. 데스크탑과 같은 공유기, 와이파이 연결
- 임베딩: Voyage AI `voyage-4-lite`, 1024차원
- HNSW: 기본 파라미터 (`m=16`, `ef_construction=64`), `hnsw.ef_search=40`
- 측정: 브라우저 개발자도구 Network 탭의 Total, [검색어 50개](data/search_terms.md) × 5회의 중앙값
- 검색어 벡터 캐시: PostgreSQL + HNSW 인덱스, 300건 기준.
  히트는 검색어 50개 × 5회, 미스는 중복을 피하기 위해 무작위 문자열 550개를 1회씩

H2와 PostgreSQL은 실행 위치가 달라 조건이 동일하지 않음.
H2는 앱과 같은 프로세스라 네트워크 비용이 없다는 점에서 자바 구현 쪽에 유리한 조건.
다만 300건 구간은 API 대기가 지배적이라 H2와 PostgreSQL의 DB 왕복 차이가 드러나지 않음.

</details>

<details>
<summary>한계</summary>

- 데이터가 300종류를 복제한 것이라 벡터 분포가 실제보다 단순함.
  전건 스캔 계열은 영향이 없지만, HNSW는 분포를 타므로 실제보다 유리하게 측정됐을 수 있음.
- 재현율: **300건 기준**, [같은 검색어 50개](data/search_terms.md)로 인덱스 적용 전후 상위 10건을 비교.
  500건 중 집합 불일치 0건. 1개('경제')에서 8·9위 순서만 교차했고 두 항목의 점수 차는 1e-4 수준.
  단, `ef_search=40`에 데이터가 300건이라 탐색 범위가 전체에 근접해 놓칠 여지가 적었음.
  속도를 측정한 19,200건은 복제 데이터라 재현율 검증에 부적합.
  따라서 이 결과를 인덱스 정확도의 일반적 근거로 보기는 어려움.
  두 조건을 따로 측정해 검색어 임베딩이 미세하게 달라진 점도 있음.
- 캐시 미스 측정에는 중복을 피하려고 무의미한 무작위 문자열을 사용.
  실제 검색어와 토큰 수가 달라 API 응답 시간에 차이가 있을 수 있음.

</details>



