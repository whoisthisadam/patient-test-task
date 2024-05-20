package com.kasperovich.patients.controller;

import com.kasperovich.patients.dto.PatientCreateDto;
import com.kasperovich.patients.dto.PatientGetDto;
import com.kasperovich.patients.dto.PatientUpdateDto;
import com.kasperovich.patients.exception.KeycloakException;
import com.kasperovich.patients.exception.NotFoundException;
import com.kasperovich.patients.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Tag(name = "Patient")
@RequestMapping(value = "/patients", produces = APPLICATION_JSON_VALUE)
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Slf4j
public class PatientController {

    PatientService patientService;

    @Operation(summary = "Create patient", description = "Create a new patient in the system",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = PatientGetDto.class),
                                    examples = {@ExampleObject(name = "response", value = "{\"id\": \"ef3211a3-600e-406f-8ab2-556cb14e645b\", \"name\": \"John Doe\", \"gender\": \"male\", \"birthDate\": \"2024-01-13T18:25:43\"}")})),
                    @ApiResponse(responseCode = "409", description = "Patient already exists"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Attempting token not permitted for creating a patient"),
                    @ApiResponse(responseCode = "502", description = "Failed to create patient in Keycloak")
            })
    @PostMapping(value = "/patient")
    @PreAuthorize("hasRole('PRACTITIONER') and hasAuthority('patient_WRITE')")
    public ResponseEntity<PatientGetDto> createPatient(@RequestBody PatientCreateDto patientCreateDto) throws KeycloakException {
        return ResponseEntity.ok(patientService.createPatient(patientCreateDto));
    }

    @Operation(summary = "Get patient by name", description = "Retrieve a patient by their name",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Patient found"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Attempting token not permitted for reading this patient"),
                    @ApiResponse(responseCode = "404", description = "Patient not found")
            })
    @GetMapping(value = "/name/{name}")
    @PreAuthorize("hasAuthority('patient_READ') and (hasRole('PRACTITIONER') or (hasRole('PATIENT') and #name.toLowerCase() == authentication.name))")
    public ResponseEntity<PatientGetDto> getByName(@PathVariable String name) throws NotFoundException {
        return ResponseEntity.ok(patientService.getPatientByName(name));
    }

    @Operation(summary = "Create multiple patients", description = "Create multiple patients in a single request",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful creation of multiple patients"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Attempting token not permitted for creating patients"),
                    @ApiResponse(responseCode = "502", description = "Failed to create patient in Keycloak")
            })
    @PostMapping
    @PreAuthorize("hasRole('PRACTITIONER') and hasAuthority('patient_WRITE')")
    public ResponseEntity<Map<String, Long>> createPatients(@RequestBody List<PatientCreateDto> patientCreateDto) throws KeycloakException {
        return ResponseEntity.ok(
                Collections.singletonMap("Total created records",
                patientService.createMultiplePatients(patientCreateDto))
        );
    }

    @Operation(summary = "Update patient", description = "Update details of an existing patient",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Patient updated successfully"),
                    @ApiResponse(responseCode = "404", description = "Patient not found")
            })
    @PutMapping("/update")
    @PreAuthorize("hasRole('PRACTITIONER') and hasAuthority('patient_WRITE')")
    public ResponseEntity<PatientGetDto> updatePatient(@RequestBody PatientUpdateDto patientUpdateDto) throws NotFoundException {
        return ResponseEntity.ok(patientService.updatePatient(patientUpdateDto));
    }


    @Operation(summary = "Delete patient", description = "Delete a patient by ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Patient deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Patient ID not found"),
                    @ApiResponse(responseCode = "502", description = "Error deleting patient from keycloak"),
            })
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('PRACTITIONER') and hasAuthority('patient_DELETE')")
    public ResponseEntity<String> deletePatient(@PathVariable String id) throws NotFoundException, KeycloakException {
        patientService.deletePatient(id);
        return new ResponseEntity<>("Patient with id " + id + " deleted", HttpStatus.NO_CONTENT);
    }


    @Operation(summary = "Delete all patients", description = "Delete all patients from the system",
            responses = {
                    @ApiResponse(responseCode = "204", description = "All patients deleted"),
                    @ApiResponse(responseCode = "502", description = "Error deleting patients from keycloak")
            })
    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('PRACTITIONER') and hasAuthority('patient_DELETE')")
    public ResponseEntity<String> deletePatients() throws KeycloakException {
        patientService.deleteAllPatients();
        return new ResponseEntity<>("All patients deleted", HttpStatus.NO_CONTENT);
    }
}
