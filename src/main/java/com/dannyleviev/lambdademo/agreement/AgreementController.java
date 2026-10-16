package com.dannyleviev.lambdademo.agreement;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/agreements")
@RequiredArgsConstructor
public class AgreementController {

    private final AgreementService service;

    @PostMapping
    public ResponseEntity<AgreementResponse> create(@Valid @RequestBody CreateAgreementRequest request,
                                                    UriComponentsBuilder uriBuilder) {
        AgreementResponse created = service.create(request);
        URI location = uriBuilder.path("/agreements/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public AgreementResponse get(@PathVariable String id) {
        return service.get(id);
    }

    @GetMapping
    public List<AgreementResponse> list() {
        return service.list();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}