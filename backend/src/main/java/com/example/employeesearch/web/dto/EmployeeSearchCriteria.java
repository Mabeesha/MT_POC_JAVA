package com.example.employeesearch.web.dto;

/** Search filters bound from query params (DESIGN §5.3). All optional. */
public record EmployeeSearchCriteria(String name, String department, String role, String status) {
}
