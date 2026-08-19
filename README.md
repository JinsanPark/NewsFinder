# 임베딩 검색 프로그램

임베딩 기반 검색 기능을 구현한 학습용 프로젝트.

## 적재 성능 - 배치 요청

300건 적재 시 58초 소요. 요청 1건당 약 200ms가 Voyage API 응답 대기 시간이었음.
`input` 파라미터가 배열을 받는 점을 이용해 128건씩 묶어 호출하도록 변경.

| | 요청 횟수 | 소요 시간 |
|---|---|---|
| 개선 전 | 300회 | 58.6초 |
| 개선 후 | 3회 | 3.5초 |

동일 데이터 기준 1회 측정. 네트워크 상황에 따라 변동 가능.

## 검색 성능 - 캐시, pgvector, 인덱스

더미 기사 300건을 복제해 2,400건, 19,200건으로 늘려가며 측정.
검색어 50개를 각 5회 요청, 응답 시간(브라우저 Network Total)의 중앙값.

| 방식 | 커밋 | 300건 | 2,400건 | 19,200건 |
|---|---|---|---|---|
| 캐시 없음 (매 검색마다 전건 조회 + 파싱) | [`c64e9bd`](https://github.com/JinsanPark/NewsFinder/commit/c64e9bd2b9e0b7a8c2a83118a8d1b9617c3e4828) | 225.3 ms | 427.2 ms | 2,136.8 ms |
| 자바 메모리 캐시 | [`d234a6a`](https://github.com/JinsanPark/NewsFinder/commit/d234a6aeb6224f504666894c63bcd84d4d84bb67) | 196.7 ms | 239.8 ms | 525.2 ms |
| pgvector (인덱스 없음) | [`95d49ec`](https://github.com/JinsanPark/NewsFinder/commit/95d49ecefc6cbe7e71825ced8ea09e0ba92fca82) | 196.8 ms | 211.2 ms | 287.4 ms |
| pgvector + HNSW 인덱스 | [`56f0197`](https://github.com/JinsanPark/NewsFinder/commit/56f01973929baad49f4891b3c9d543790236156e) + 아래 인덱스 | — | — | 209.2 ms |

응답 시간에는 검색어를 벡터로 바꾸는 Voyage API 왕복(약 200ms)이 포함되어 있음.
이를 빼면 건당 비용은 캐시 없음 100μs, 자바 캐시 17μs, pgvector 5μs.


인덱스 측정은 19,200건만 수행.

### 한계

- 데이터가 300종류를 복제한 것이라 벡터 분포가 실제보다 단순함.
  전건 스캔 계열은 영향이 없지만, HNSW는 분포를 타므로 실제보다 유리하게 측정됐을 수 있음.
- 인덱스 적용 전후로 상위 10건은 동일. 검색어 '경제' 기준으로, 8·9위 순서만 뒤바뀌었고 두 점수 차는 0.00007.

### 인덱스 생성

```sql
CREATE INDEX news_embedding_hnsw ON news USING hnsw (embedding vector_cosine_ops);
```

이 인덱스는 코드로 관리되지 않으므로 새 환경에서는 직접 실행해야 함.