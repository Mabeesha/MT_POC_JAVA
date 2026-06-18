package com.example.employeesearch.service;

import com.example.employeesearch.domain.Employee;
import com.example.employeesearch.repository.EmployeeRepository;
import com.example.employeesearch.repository.EmployeeSpecifications;
import com.example.employeesearch.web.dto.EmployeeDto;
import com.example.employeesearch.web.dto.EmployeeSearchCriteria;
import com.example.employeesearch.web.dto.FilterOptionsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Employee search (BR-4) — the single source of the search predicate, reused by E1 and E2
 * (DESIGN §5.4, §14.2). Also serves the fixed filter domains (BR-10, D6).
 */
@Service
public class EmployeeService {

    private static final Logger AUDIT = LoggerFactory.getLogger("com.example.employeesearch.audit");

    /** Fixed domains (BR-10, A-1). Each is prefixed with "All" (no filter). */
    private static final List<String> DEPARTMENTS =
            List.of("All", "Finance", "HR", "IT", "Marketing", "Operations");
    private static final List<String> ROLES =
            List.of("All", "Analyst", "Coordinator", "Designer", "Developer", "Manager");
    private static final List<String> STATUSES =
            List.of("All", "Active", "Inactive", "On Leave");

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /** Search with AND-combined predicates, always sorted by Name ascending (BR-4, FR-3). */
    public List<EmployeeDto> search(EmployeeSearchCriteria criteria) {
        Specification<Employee> spec = Specification.allOf(
                EmployeeSpecifications.nameLike(criteria.name()),
                EmployeeSpecifications.departmentEquals(criteria.department()),
                EmployeeSpecifications.roleEquals(criteria.role()),
                EmployeeSpecifications.statusEquals(criteria.status()));

        List<EmployeeDto> results = employeeRepository
                .findAll(spec, Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(EmployeeDto::from)
                .toList();

        AUDIT.info("search name=\"{}\" department=\"{}\" role=\"{}\" status=\"{}\" results={}",
                criteria.name(), criteria.department(), criteria.role(), criteria.status(), results.size());
        return results;
    }

    public FilterOptionsDto filterOptions() {
        return new FilterOptionsDto(
                new ArrayList<>(DEPARTMENTS),
                new ArrayList<>(ROLES),
                new ArrayList<>(STATUSES));
    }
}
