package com.apsis.counterservice.service;

import com.apsis.counterservice.api.model.Counter;
import com.apsis.counterservice.exception.ResourceConflictException;
import com.apsis.counterservice.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class CounterService {

    private Map<String, Integer> counterMap = new ConcurrentHashMap<>();

    public Mono<Counter> createCounter(@Valid Counter counter) {
        return Mono.fromCallable(() -> {
            if (counterMap.get(counter.getName()) != null) {
                throw new ResourceConflictException(String.format("%s already exists", counter.getName()));
            }
            counterMap.put(counter.getName(), counter.getValue());
            return Counter.builder()
                    .name(counter.getName())
                    .value(counter.getValue())
                    .build();
        });
    }

    public Flux<Counter> getCounters() {
        return Flux.fromIterable(counterMap
                .entrySet()
                .stream()
                .map(counterEntry -> Counter
                        .builder()
                        .name(counterEntry.getKey())
                        .value(counterEntry.getValue())
                        .build()
                ).collect(Collectors.toList())
        );
    }


    public Mono<Counter> getCounter(String counterName) {
        return Mono.fromCallable(() -> {
            Integer value = counterMap.get(counterName);
            if (value == null) {
                throw new ResourceNotFoundException(String.format("%s not found", counterName));
            }
            return Counter
                    .builder()
                    .name(counterName)
                    .value(value)
                    .build();
        });
    }

    public Mono<Counter> updateCounter(String counterName) {
        return Mono.fromCallable(() -> {
            Integer value = counterMap.computeIfPresent(counterName, (s, integer) -> ++integer);
            if (value == null) {
                throw new ResourceNotFoundException(String.format("%s not found", counterName));
            }
            counterMap.put(counterName, value);
            return Counter
                    .builder()
                    .name(counterName)
                    .value(value)
                    .build();
        });
    }
}
