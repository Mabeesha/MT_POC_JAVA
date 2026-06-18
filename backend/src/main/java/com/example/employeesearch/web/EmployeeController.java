package com.example.employeesearch.web;

import com.example.employeesearch.security.Roles;
import com.example.employeesearch.service.CsvExportService;
import com.example.employeesearch.service.EmployeeService;
import com.example.employeesearch.web.dto.EmployeeDto;
import com.example.employeesearch.web.dto.EmployeeSearchCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Employee endpoints E1 (search) and E2 (export) (DESIGN §5.2). */
@RestController
@RequestMapping("/api/employees")
@PreAuthorize("hasRole('" + Roles.EMPLOYEE_VIEWER_NAME + "')")
public class EmployeeController {

    private static final Logger AUDIT = LoggerFactory.getLogger("com.example.employeesearch.audit");
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final EmployeeService employeeService;
    private final CsvExportService csvExportService;

    public EmployeeController(EmployeeService employeeService, CsvExportService csvExportService) {
        this.employeeService = employeeService;
        this.csvExportService = csvExportService;
    }

    /** E1 — Search (FR-3, BR-4). Filters bound from query params; results sorted by Name asc. */
    @GetMapping
    public List<EmployeeDto> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        return employeeService.search(new EmployeeSearchCriteria(name, department, role, status));
    }

    /** E2 — Export current result set as CSV (FR-5, EXP-1, BR-7/8). Empty result → 409 via handler. */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        List<EmployeeDto> employees =
                employeeService.search(new EmployeeSearchCriteria(name, department, role, status));
        byte[] csv = csvExportService.toCsv(employees); // throws EmptyExportException -> 409

        String filename = "employees_" + LocalDateTime.now().format(TS) + ".csv";
        AUDIT.info("export count={} filename=\"{}\"", employees.size(), filename);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(csv);
    }
}
