package com.proteccio.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TagOverrideRequest {
    @NotNull
    private String sensitivityTag;
}
