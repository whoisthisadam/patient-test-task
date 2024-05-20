package com.kasperovich.patients.model;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Entity
@Table(name = "patients")
@Getter
@Setter
@FieldDefaults(level = PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Patient {

    public enum PATIENT_GENDER {
        male, female, other
    }

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    UUID id;

    @Column(nullable = false, length = 256, unique = true)
    String name;

    @Column(length = 128)
    @Enumerated(EnumType.STRING)
    PATIENT_GENDER gender;

    @Column(name = "birth_date", nullable = false)
    LocalDateTime birthDate = LocalDateTime.now();

}
