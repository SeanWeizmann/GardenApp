package com.example.GardenersApp.api;

import com.example.GardenersApp.domain.GoogleAccount;
import com.example.GardenersApp.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/customers", "/api/clients"})
public class CustomerController {
    private final CustomerService service;

    public CustomerController(CustomerService s) {
        service = s;
    }

    @GetMapping
    public List<CustomerResponse> list(@RequestAttribute("authenticatedGoogleAccount") GoogleAccount owner, @RequestParam(required = false) String q) {
        return service.list(owner, q).stream().map(CustomerResponse::from).toList();
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@RequestAttribute("authenticatedGoogleAccount") GoogleAccount owner, @PathVariable Long id) {
        return CustomerResponse.from(service.get(owner, id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(@RequestAttribute("authenticatedGoogleAccount") GoogleAccount owner, @Valid @RequestBody CustomerRequest r) {
        return CustomerResponse.from(service.create(owner, r));
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@RequestAttribute("authenticatedGoogleAccount") GoogleAccount owner, @PathVariable Long id, @Valid @RequestBody CustomerRequest r) {
        return CustomerResponse.from(service.update(owner, id, r));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestAttribute("authenticatedGoogleAccount") GoogleAccount owner, @PathVariable Long id) {
        service.delete(owner, id);
    }
}
