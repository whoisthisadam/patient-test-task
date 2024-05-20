package com.kasperovich.patients.repo;

import com.kasperovich.patients.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PatientRepo extends JpaRepository<Patient, UUID> {

    Optional<Patient> findByName(String name);
}
