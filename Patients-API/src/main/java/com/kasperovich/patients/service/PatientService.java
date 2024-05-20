package com.kasperovich.patients.service;

import com.kasperovich.patients.dto.PatientCreateDto;
import com.kasperovich.patients.dto.PatientGetDto;
import com.kasperovich.patients.dto.PatientUpdateDto;
import com.kasperovich.patients.exception.KeycloakException;
import com.kasperovich.patients.exception.NotFoundException;

import java.util.List;

public interface PatientService {

    PatientGetDto createPatient(PatientCreateDto patientCreateDto) throws KeycloakException;

    long createMultiplePatients(List<PatientCreateDto> patientCreateDtos) throws KeycloakException;

    PatientGetDto getPatientByName(String name) throws NotFoundException;

    PatientGetDto updatePatient(PatientUpdateDto patientUpdateDto) throws NotFoundException;

    boolean deletePatient(String id) throws NotFoundException, KeycloakException;

    void deleteAllPatients() throws KeycloakException;

}
