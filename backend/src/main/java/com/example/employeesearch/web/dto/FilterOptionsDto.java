package com.example.employeesearch.web.dto;

import java.util.List;

/** Fixed dropdown domains for the SPA (BR-10, D6, DESIGN §5.2 M1). Each list is prefixed with "All". */
public record FilterOptionsDto(
        List<String> departments,
        List<String> roles,
        List<String> statuses) {
}
