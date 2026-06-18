package com.example.employeesearch.service;

import com.example.employeesearch.web.dto.EmployeeDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Builds the employee CSV (EXP-1, BR-8). Owns all CSV rules in one place (DESIGN §14.2):
 * column order, RFC-4180-style quoting, raw numeric salary, and CRLF line endings.
 * UTF-8, no BOM (OQ-5 design decision).
 */
@Service
public class CsvExportService {

    private static final String CRLF = "\r\n";
    private static final String[] HEADERS =
            {"Id", "Name", "Department", "Role", "Status", "Email", "Phone", "HireDate", "Salary"};

    /**
     * @throws EmptyExportException when the list is empty (BR-7 guard).
     */
    public byte[] toCsv(List<EmployeeDto> employees) {
        if (employees == null || employees.isEmpty()) {
            throw new EmptyExportException("There are no rows to export.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", HEADERS)).append(CRLF);

        for (EmployeeDto e : employees) {
            sb.append(field(e.id() == null ? "" : e.id().toString())).append(',')
                    .append(field(e.name())).append(',')
                    .append(field(e.department())).append(',')
                    .append(field(e.role())).append(',')
                    .append(field(e.status())).append(',')
                    .append(field(e.email())).append(',')
                    .append(field(e.phone())).append(',')
                    .append(field(e.hireDate())).append(',')
                    .append(field(salary(e.salary())))
                    .append(CRLF);
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /** Raw numeric salary (no currency formatting), e.g. {@code 85000} (EXP-1 col 9, BR-6). */
    private static String salary(BigDecimal salary) {
        if (salary == null) {
            return "";
        }
        return salary.stripTrailingZeros().toPlainString();
    }

    /**
     * BR-8 quoting: wrap in double-quotes if the field contains a comma, double-quote, or newline;
     * internal double-quotes are doubled. Otherwise written as-is.
     */
    private static String field(String value) {
        if (value == null) {
            return "";
        }
        boolean mustQuote = value.contains(",") || value.contains("\"") || value.contains("\n");
        if (!mustQuote) {
            return value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
