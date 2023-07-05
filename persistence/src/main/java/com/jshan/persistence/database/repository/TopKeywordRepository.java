package com.jshan.persistence.database.repository;

import com.jshan.persistence.database.entity.TopKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopKeywordRepository extends JpaRepository<TopKeyword, Integer> {

}
