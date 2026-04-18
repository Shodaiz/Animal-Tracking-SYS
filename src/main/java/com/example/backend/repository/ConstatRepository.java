package com.example.backend.repository;


import com.example.backend.model.Constat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConstatRepository extends JpaRepository<Constat, Long> {
}