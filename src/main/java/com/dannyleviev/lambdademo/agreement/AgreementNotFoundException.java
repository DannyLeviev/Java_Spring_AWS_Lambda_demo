package com.dannyleviev.lambdademo.agreement;

public class AgreementNotFoundException extends RuntimeException {
    public AgreementNotFoundException(String id) {
        super("Agreement " + id + " not found");
    }
}