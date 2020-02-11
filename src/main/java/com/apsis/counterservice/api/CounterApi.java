package com.apsis.counterservice.api;

import com.apsis.counterservice.api.model.Counter;
import com.apsis.counterservice.api.model.CounterError;
import com.apsis.counterservice.exception.ResourceConflictException;
import com.apsis.counterservice.exception.ResourceNotFoundException;
import com.apsis.counterservice.service.CounterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

@Api(tags = "Counter Service")
@Validated
@RequiredArgsConstructor
@RestController
public class CounterApi {

    private final CounterService counterService;

    @ApiOperation(value = "Create new counter")
    @ApiResponses({
            @ApiResponse(code = HTTP_BAD_REQUEST, message = "Invalid request", response = CounterError.class),
            @ApiResponse(code = HTTP_CONFLICT, message = "Resource Conflict", response = CounterError.class)
    })
    @PostMapping(value = "/api/counters", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Counter> createCounter(@Valid @RequestBody Counter counter) {
        return counterService.createCounter(counter);
    }

    @ApiOperation(value = "get all counters")
    @GetMapping(value = "/api/counters", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Counter> getCounters() {
        return counterService.getCounters();
    }

    @ApiOperation(value = "get counter")
    @ApiResponses({
            @ApiResponse(code = HTTP_NOT_FOUND, message = "resource not found", response = CounterError.class)
    })
    @GetMapping(value = "/api/counters/{counter-name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Counter> getCounter(@NotEmpty @PathVariable("counter-name") String counterName) {
        return counterService.getCounter(counterName);
    }

    @ApiOperation(value = "update counter")
    @ApiResponses({
            @ApiResponse(code = HTTP_NOT_FOUND, message = "resource not found", response = CounterError.class)
    })
    @PutMapping(value = "/api/counters/{counter-name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Counter> updateCounter(@NotEmpty @PathVariable("counter-name") String counterName) {
        return counterService.updateCounter(counterName);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CounterError> handleNotFoundError(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                CounterError
                        .builder()
                        .error("resource_not_found")
                        .errorDescription(e.getMessage())
                        .build());
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<CounterError> handleConflictError(ResourceConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(CounterError
                        .builder()
                        .error("resource_conflict")
                        .errorDescription(e.getMessage())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CounterError> handleValidationError(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CounterError
                        .builder()
                        .error("validation_error")
                        .errorDescription(result.getFieldErrors()
                                .stream()
                                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                                .reduce((s, s2) -> s + "," + s2)
                                .orElse("invalid input"))
                        .build());
    }
}
