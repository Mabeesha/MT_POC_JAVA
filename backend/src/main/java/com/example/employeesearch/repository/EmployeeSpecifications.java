package com.example.employeesearch.repository;

import com.example.employeesearch.domain.Employee;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * Single source of the employee search predicate (BR-4, DESIGN §5.5).
 * Reused by both search (E1) and export (E2) so filter logic is never duplicated.
 *
 * <p>Semantics preserved from the desktop app:
 * <ul>
 *   <li>name → {@code LIKE %trim(name)%} (native SQLite LIKE; R-2/OQ-4 not "fixed")</li>
 *   <li>department/role/status → exact {@code =}, ignored when blank or {@code "All"}</li>
 *   <li>active predicates combined with AND</li>
 * </ul>
 */
public final class EmployeeSpecifications {

    public static final String ALL = "All";

    private EmployeeSpecifications() {
    }

    public static Specification<Employee> nameLike(String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        String pattern = "%" + name.trim() + "%";
        return (root, query, cb) -> cb.like(root.get("name"), pattern);
    }

    public static Specification<Employee> departmentEquals(String department) {
        return exactUnlessAll("department", department);
    }

    public static Specification<Employee> roleEquals(String role) {
        return exactUnlessAll("role", role);
    }

    public static Specification<Employee> statusEquals(String status) {
        return exactUnlessAll("status", status);
    }

    private static Specification<Employee> exactUnlessAll(String field, String value) {
        if (!StringUtils.hasText(value) || ALL.equals(value)) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get(field), value);
    }
}
