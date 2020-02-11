package com.apsis.counterservice.api.model;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder(toBuilder = true)
public class Counter {

    @NotBlank(message = "name cannot be blank")
    @NotNull(message = "name cannot be null")
    private String name;

    @Range(min = 0, message = "counter value cannot be less than 0")
    private int value;
}
