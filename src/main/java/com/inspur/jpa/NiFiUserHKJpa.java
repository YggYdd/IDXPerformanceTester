package com.inspur.jpa;

import com.inspur.bean.NiFiUserHK;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NiFiUserHKJpa extends JpaRepository<NiFiUserHK, String> {

    NiFiUserHK findByUserId(String userId);
}
