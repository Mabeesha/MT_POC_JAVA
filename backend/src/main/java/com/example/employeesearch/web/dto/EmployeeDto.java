package com.example.employeesearch.web.dto;

import com.example.employeesearch.domain.Employee;

import java.math.BigDecimal;

/**
 * Employee API representation (DESIGN §5.3, D7). {@code salary} is the raw numeric value;
 * currency formatting is a frontend concern (BR-6). No {@code salaryFormatted} field.
 */
public record EmployeeDto(
        Long id,
        String name,
        String department,
        String role,
        String status,
        String email,
        String phone,
        String hireDate,
        BigDecimal salary) {

    public static EmployeeDto from(Employee e) {
        return new EmployeeDto(
                e.getId(),
                e.getName(),
                e.getDepartment(),
                e.getRole(),
                e.getStatus(),
                e.getEmail(),
                e.getPhone(),
                e.getHireDate(),
                e.getSalary());
    }
}
