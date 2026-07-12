package com.example.shorty.repository;

import com.example.shorty.model.UrlEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepository extends CrudRepository<UrlEntity, Long> {
    Optional<UrlEntity> findByShortCode(String shortCode);
    Optional<UrlEntity> findByLongUrl(String longUrl);

    @Query("SELECT nextval('urls_id_seq')")
    Long nextId();
}
