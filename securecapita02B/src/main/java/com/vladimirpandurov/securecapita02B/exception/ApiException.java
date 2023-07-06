package com.vladimirpandurov.securecapita02B.exception;

public class ApiException extends RuntimeException{
    public ApiException(String message){
        super(message);
    }
}
