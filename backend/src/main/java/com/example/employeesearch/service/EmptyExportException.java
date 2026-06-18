package com.example.employeesearch.service;

/** Raised when an export is requested with no rows in the result set (BR-7 → HTTP 409). */
public class EmptyExportException extends RuntimeException {

    public EmptyExportException(String message) {
        super(message);
    }
}
