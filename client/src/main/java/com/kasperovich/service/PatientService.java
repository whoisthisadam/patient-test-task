package com.kasperovich.service;

import com.kasperovich.config.PatientProperties;
import com.kasperovich.dto.PatientCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static lombok.AccessLevel.PRIVATE;

@Service
@FieldDefaults(level = PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    WebClient webClient;

    public void addTestPatients(int number) {
         webClient.post()
                .uri("")
                .bodyValue(createTestRecords(number))
                .retrieve()
                .bodyToMono(Map.class)
                 .doOnNext(map-> log.info(map.toString()))
                .block();
    }

    private List<PatientCreateRequest> createTestRecords(int number) {
        List<PatientCreateRequest> patients = new ArrayList<>();
        Random random = new Random();
        String[] genders = {"male", "female", "other"};

        for (int i = 10; i < number+10; i++) {
            String name = "patient" + i;
            String password = "P@ssw0rd" + i;
            String gender = genders[random.nextInt(genders.length)];
            String birthDate = LocalDateTime.now().toString();

            patients.add(new PatientCreateRequest(name, password, gender, birthDate));
        }

        return patients;
    }


}
