package com.example.employeesearch.service;

import com.example.employeesearch.domain.Employee;
import com.example.employeesearch.repository.EmployeeRepository;
import com.example.employeesearch.web.dto.EmployeeDto;
import com.example.employeesearch.web.dto.EmployeeSearchCriteria;
import com.example.employeesearch.web.dto.FilterOptionsDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Employee search wiring (BR-4, FR-3) and the fixed filter domains (BR-10, D6). */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void search_alwaysSortsByNameAscending() {
        when(employeeRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of());

        employeeService.search(new EmployeeSearchCriteria(null, null, null, null));

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(employeeRepository).findAll(any(Specification.class), sortCaptor.capture());
        assertThat(sortCaptor.getValue()).isEqualTo(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Test
    void search_mapsEntitiesToDtos() {
        Employee entity = mockEmployee();
        when(employeeRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(entity));

        List<EmployeeDto> results =
                employeeService.search(new EmployeeSearchCriteria("ali", "IT", "Developer", "Active"));

        assertThat(results).hasSize(1);
        EmployeeDto dto = results.get(0);
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Alice Johnson");
        assertThat(dto.department()).isEqualTo("IT");
        assertThat(dto.salary()).isEqualByComparingTo("85000");
    }

    @Test
    void search_emptyResult_returnsEmptyList() {
        when(employeeRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of());

        assertThat(employeeService.search(new EmployeeSearchCriteria("zzz", null, null, null)))
                .isEmpty();
    }

    @Test
    void filterOptions_eachDomainIsPrefixedWithAll() {
        FilterOptionsDto options = employeeService.filterOptions();

        assertThat(options.departments()).first().isEqualTo("All");
        assertThat(options.roles()).first().isEqualTo("All");
        assertThat(options.statuses()).first().isEqualTo("All");
        assertThat(options.statuses()).contains("On Leave");
    }

    private static Employee mockEmployee() {
        Employee e = org.mockito.Mockito.mock(Employee.class);
        lenient().when(e.getId()).thenReturn(1L);
        lenient().when(e.getName()).thenReturn("Alice Johnson");
        lenient().when(e.getDepartment()).thenReturn("IT");
        lenient().when(e.getRole()).thenReturn("Developer");
        lenient().when(e.getStatus()).thenReturn("Active");
        lenient().when(e.getEmail()).thenReturn("alice@corp.com");
        lenient().when(e.getPhone()).thenReturn("555-0101");
        lenient().when(e.getHireDate()).thenReturn("2021-03-15");
        lenient().when(e.getSalary()).thenReturn(new BigDecimal("85000"));
        return e;
    }
}
