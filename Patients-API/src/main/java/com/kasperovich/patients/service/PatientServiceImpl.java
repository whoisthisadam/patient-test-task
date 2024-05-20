package com.kasperovich.patients.service;

import com.kasperovich.patients.dto.PatientCreateDto;
import com.kasperovich.patients.dto.PatientGetDto;
import com.kasperovich.patients.dto.PatientUpdateDto;
import com.kasperovich.patients.exception.KeycloakException;
import com.kasperovich.patients.exception.NotFoundException;
import com.kasperovich.patients.model.Patient;
import com.kasperovich.patients.repo.PatientRepo;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Service
public class PatientServiceImpl implements PatientService {

    PatientRepo patientRepo;

    KeycloakService keycloakService;

    @Override
    public PatientGetDto createPatient(PatientCreateDto patientCreateDto) throws KeycloakException {
        //Create a user in Keycloak
        log.debug("Creating a patient user in Keycloak");
        String userId = keycloakService.createPatient(patientCreateDto.name(), patientCreateDto.password());

        Patient patient = Patient
                .builder()
                //Set the user id from Keycloak
                .id(UUID.fromString(userId))
                .name(patientCreateDto.name())
                .gender(Patient.PATIENT_GENDER.valueOf(patientCreateDto.gender()))
                .birthDate(LocalDateTime.parse(patientCreateDto.birthDate()))
                .build();

        //Save the patient in the database
        Patient savedPatient = patientRepo.save(patient);

        return PatientGetDto
                .builder()
                .id(savedPatient.getId().toString())
                .name(savedPatient.getName())
                .gender(savedPatient.getGender().name())
                .birthDate(savedPatient.getBirthDate().toString())
                .build();
    }

    @Override
    public long createMultiplePatients(List<PatientCreateDto> patientCreateDtos) throws KeycloakException {
        log.debug("Creating multiple patients");
        int createdPatients = 0;
        for (PatientCreateDto patientCreateDto : patientCreateDtos) {
                createPatient(patientCreateDto);
                createdPatients++;
        }
        return createdPatients;
    }

    @Override
    public PatientGetDto getPatientByName(String name) throws NotFoundException {
        log.debug("Searching patient by name {}", name);
        Patient patient = patientRepo.findByName(name)
                .orElseThrow(() -> new NotFoundException("Patient with name " + name + " not found"));
        log.debug("Patient found: {}", patient);

        return PatientGetDto
                .builder()
                .id(patient.getId().toString())
                .name(patient.getName())
                .gender(patient.getGender().name())
                .birthDate(patient.getBirthDate().toString())
                .build();
    }

    @Override
    public PatientGetDto updatePatient(PatientUpdateDto patientUpdateDto) throws NotFoundException {
        log.debug("Searching patient by id {}", patientUpdateDto.id());
        Patient patient = patientRepo.findById(UUID.fromString(patientUpdateDto.id()))
                .orElseThrow(() -> new NotFoundException("Patient with id " + patientUpdateDto.id() + " not found"));
        log.debug("Patient found: {}", patient);

        patient.setName(patientUpdateDto.name());
        patient.setGender(Patient.PATIENT_GENDER.valueOf(patientUpdateDto.gender()));
        patient.setBirthDate(LocalDateTime.parse(patientUpdateDto.birthDate()));

        log.debug("Updating patient: {}", patient);
        Patient savedPatient = patientRepo.save(patient);

        log.debug("Updated patient: {}", savedPatient);

        return PatientGetDto
                .builder()
                .id(savedPatient.getId().toString())
                .name(savedPatient.getName())
                .gender(savedPatient.getGender().name())
                .birthDate(savedPatient.getBirthDate().toString())
                .build();

    }

    @Override
    public boolean deletePatient(String id) throws NotFoundException, KeycloakException {
        log.debug("Searching patient by id {}", id);
        Patient patient = patientRepo.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Patient with id " + id + " not found"));
        log.debug("Patient found: {}", patient);

        log.debug("Deleting patient: {}", patient);
        patientRepo.delete(patient);
        log.debug("Patient deleted: {}", patient);

        log.debug("Deleting patient from Keycloak: {}", patient);
        keycloakService.deletePatientByUsername(patient.getName().toLowerCase());

        return true;

    }

    @Override
    public void deleteAllPatients() throws KeycloakException {
        log.debug("Deleting all patients");
        List<Patient> patients = patientRepo.findAll();

        for(Patient patient : patients){
            patientRepo.delete(patient);
            keycloakService.deletePatientByUsername(patient.getName().toLowerCase());
        }
        log.debug("All patients deleted");
    }


}
