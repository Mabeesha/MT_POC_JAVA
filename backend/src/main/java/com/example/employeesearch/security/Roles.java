package com.example.employeesearch.security;

/**
 * Application roles (DESIGN §7.3). Single implicit role today: the authenticated employee viewer.
 * Maps to AD group {@code EmployeeSearch-Users} — TODO (AD).
 */
public final class Roles {

    /** Granted authority value (Spring expects the {@code ROLE_} prefix). */
    public static final String EMPLOYEE_VIEWER = "ROLE_EMPLOYEE_VIEWER";

    /** Bare role name used with {@code @PreAuthorize("hasRole(...)")}. */
    public static final String EMPLOYEE_VIEWER_NAME = "EMPLOYEE_VIEWER";

    private Roles() {
    }
}
