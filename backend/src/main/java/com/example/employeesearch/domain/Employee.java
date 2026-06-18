package com.example.employeesearch.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

/**
 * JPA entity mapped onto the existing {@code Employees} table (C1, DESIGN §4.2).
 * Exact DB column casing is preserved. {@code hireDate} stays {@code String} (D4);
 * {@code salary} is read as {@code BigDecimal} over the {@code REAL} column (D3, BR-6).
 */
@Entity
@Table(name = "Employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JdbcTypeCode(SqlTypes.INTEGER)   // SQLite INTEGER affinity (C1, R-1); keep Long in Java
    @Column(name = "Id")
    private Long id;

    @Column(name = "Name", nullable = false)
    private String name;

    @Column(name = "Department", nullable = false)
    private String department;

    @Column(name = "Role", nullable = false)
    private String role;

    @Column(name = "Status", nullable = false)
    private String status;

    @Column(name = "Email")
    private String email;

    @Column(name = "Phone")
    private String phone;

    @Column(name = "HireDate")
    private String hireDate;

    @JdbcTypeCode(SqlTypes.REAL)      // SQLite REAL affinity (C1, R-1, D3); read into BigDecimal
    @Column(name = "Salary")
    private BigDecimal salary;

    protected Employee() {
        // for JPA
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getHireDate() {
        return hireDate;
    }

    public BigDecimal getSalary() {
        return salary;
    }
}
