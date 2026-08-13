package com.poc.oauth.resourceserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poc.oauth.resourceserver.entity.EmpleadoEntity;

public interface EmpleadoRepository extends JpaRepository<EmpleadoEntity, Long> {
}