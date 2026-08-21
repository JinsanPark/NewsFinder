package org.jin.newsfinder.queryVector;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface QueryVectorCacheRepository extends JpaRepository<QueryVectorCache, Long> {
    Optional<QueryVectorCache> findByNormalizedQueryAndModel(String normalizedQuery, String model);
}
