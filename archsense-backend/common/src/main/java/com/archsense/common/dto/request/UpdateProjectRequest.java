package com.archsense.common.dto.request;

import com.archsense.common.dto.ProjectConstraints;
import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(
        @Size(min = 3, max = 100, message = "Project name must be between 3 and 100 characters")
        String name,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,

        ProjectConstraints constraints
) {
}