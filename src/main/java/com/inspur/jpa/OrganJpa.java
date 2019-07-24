package com.inspur.jpa;

import com.inspur.bean.Organ;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OrganJpa extends JpaRepository<Organ, String> {
}
