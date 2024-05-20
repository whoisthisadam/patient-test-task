package com.kasperovich.patients.exception;

import lombok.Builder;

@Builder
public record ErrorContainer(String exceptionId, String errorMessage, int errorCode, String e){
}
