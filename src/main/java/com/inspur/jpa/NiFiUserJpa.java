package com.inspur.jpa;

import com.inspur.bean.NiFiUser;
import org.springframework.data.jpa.repository.JpaRepository;


public interface NiFiUserJpa extends JpaRepository<NiFiUser, String> {
    NiFiUser findByUserId(String userId);
}
