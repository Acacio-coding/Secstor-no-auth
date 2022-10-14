package com.ifsc.secstor.api.repository;


import com.ifsc.secstor.api.model.NumberModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NumberRepository extends JpaRepository<NumberModel, Long> {
    @Query(value = "SELECT COUNT(*) FROM tb_pvss_numbers", nativeQuery = true)
    int isEmpty();
}
