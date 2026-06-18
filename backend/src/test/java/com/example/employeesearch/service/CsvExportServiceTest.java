package com.example.employeesearch.service;

import com.example.employeesearch.web.dto.EmployeeDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvExportServiceTest {

    private final CsvExportService service = new CsvExportService();

    @Test
    void emitsHeaderAndRowInExpOneColumnOrderWithRawSalaryAndCrlf() {
        EmployeeDto e = new EmployeeDto(1L, "Alice Johnson", "IT", "Developer", "Active",
                "alice@corp.com", "555-0101", "2021-03-15", new BigDecimal("85000.0"));

        String csv = new String(service.toCsv(List.of(e)), StandardCharsets.UTF_8);

        assertThat(csv).isEqualTo(
                "Id,Name,Department,Role,Status,Email,Phone,HireDate,Salary\r\n"
                        + "1,Alice Johnson,IT,Developer,Active,alice@corp.com,555-0101,2021-03-15,85000\r\n");
    }

    @Test
    void noBomIsEmitted() {
        EmployeeDto e = new EmployeeDto(1L, "A", "IT", "Dev", "Active", "", "", "", BigDecimal.ZERO);
        byte[] bytes = service.toCsv(List.of(e));
        // UTF-8 BOM would be EF BB BF (OQ-5: design decision = no BOM).
        assertThat(bytes[0]).isNotEqualTo((byte) 0xEF);
        assertThat(new String(bytes, StandardCharsets.UTF_8)).startsWith("Id,");
    }

    @Test
    void quotesFieldsContainingCommaQuoteOrNewline_doublingInnerQuotes() {
        EmployeeDto e = new EmployeeDto(2L, "Doe, John \"JD\"", "R&D\nLab", "Dev", "Active",
                "x@y.com", "1", "2020-01-01", new BigDecimal("60000"));

        String csv = new String(service.toCsv(List.of(e)), StandardCharsets.UTF_8);
        String dataLine = csv.split("\r\n")[1];

        assertThat(dataLine).contains("\"Doe, John \"\"JD\"\"\"");
        assertThat(dataLine).contains("\"R&D\nLab\"");
        // A plain field is left unquoted.
        assertThat(dataLine).contains(",x@y.com,");
    }

    @Test
    void emptyListThrows() {
        assertThatThrownBy(() -> service.toCsv(List.of()))
                .isInstanceOf(EmptyExportException.class)
                .hasMessage("There are no rows to export.");
    }
}
