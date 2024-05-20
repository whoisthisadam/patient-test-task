package com.kasperovich.patients.dto;

import lombok.Builder;

@Builder
public record PatientGetDto(String id, String name, String gender, String birthDate) {
}
