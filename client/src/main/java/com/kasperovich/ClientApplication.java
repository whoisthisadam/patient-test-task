package com.kasperovich;

import com.kasperovich.service.PatientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class ClientApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    @Autowired
    PatientService patientService;

    @Value("${generate.records-number}")
    Integer recordsNumber;

    @Override
    public void run(String... args) {
        patientService.addTestPatients(recordsNumber);
    }
}