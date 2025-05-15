package com.beour.user.dto;


public class CheckDuplicateResponse {
    private boolean duplicated;
    private String message;

    public CheckDuplicateResponse(boolean duplicated, String message) {
        this.duplicated = duplicated;
        this.message = message;
    }

    public boolean isDuplicated(){
        return duplicated;
    }

    public String getMessage(){
        return message;
    }
}
