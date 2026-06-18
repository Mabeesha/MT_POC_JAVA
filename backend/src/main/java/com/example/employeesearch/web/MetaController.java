package com.example.employeesearch.web;

import com.example.employeesearch.security.Roles;
import com.example.employeesearch.service.EmployeeService;
import com.example.employeesearch.web.dto.FilterOptionsDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Metadata endpoint M1 — fixed filter dropdown domains (BR-10, D6, DESIGN §5.2). */
@RestController
@RequestMapping("/api/meta")
@PreAuthorize("hasRole('" + Roles.EMPLOYEE_VIEWER_NAME + "')")
public class MetaController {

    private final EmployeeService employeeService;

    public MetaController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/filters")
    public FilterOptionsDto filters() {
        return employeeService.filterOptions();
    }
}
