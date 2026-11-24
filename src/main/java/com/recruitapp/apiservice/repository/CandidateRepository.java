package com.recruitapp.apiservice.repository;

import com.recruitapp.apiservice.domain.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CandidateRepository
        extends JpaRepository<Candidate, Long>, JpaSpecificationExecutor<Candidate> {

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailAndIdNot(String email, Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Candidate c WHERE c.id = :id")
    int deleteCandidateById(@Param("id") Long id);
}
