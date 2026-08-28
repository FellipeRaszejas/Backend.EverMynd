package com.evermynd.doctor.exception;

public class DoctorProfileNotFoundException extends RuntimeException {
    public DoctorProfileNotFoundException(String message) {
        super(message);
    }
}