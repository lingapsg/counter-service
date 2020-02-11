package com.apsis.counterservice.api.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class CounterError {

    private String error;
    private String errorDescription;
}
