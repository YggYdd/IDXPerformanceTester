package com.inspur.jpa;

import com.inspur.bean.Organ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface OrganJpa extends JpaRepository<Organ, String> {
    @Query(value = "select id from bde_nifi_task where user_id = ?1", nativeQuery = true)
    List<String> findAllIdByUserId(String userId);
}
