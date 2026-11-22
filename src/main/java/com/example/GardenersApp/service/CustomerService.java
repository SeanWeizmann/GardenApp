package com.example.GardenersApp.service;

import com.example.GardenersApp.api.CustomerRequest;
import com.example.GardenersApp.domain.Customer;
import com.example.GardenersApp.domain.GoogleAccount;
import com.example.GardenersApp.repository.CustomerRepository;
import com.example.GardenersApp.repository.GardenTaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class CustomerService {
    private final CustomerRepository customers;
    private final GardenTaskRepository tasks;

    public CustomerService(CustomerRepository customers, GardenTaskRepository tasks) {
        this.customers = customers;
        this.tasks = tasks;
    }

    public List<Customer> list(GoogleAccount owner, String query) {
        return query == null || query.isBlank()
                ? customers.findAllByOwnerIdOrderByNameAsc(owner.getId())
                : customers.search(owner.getId(), query.trim());
    }

    public Customer get(GoogleAccount owner, Long id) {
        return customers.findByIdAndOwnerId(id, owner.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    public Customer create(GoogleAccount owner, CustomerRequest request) {
        Customer customer = new Customer();
        customer.setOwner(owner);
        apply(customer, request);
        return customers.save(customer);
    }

    public Customer update(GoogleAccount owner, Long id, CustomerRequest request) {
        Customer customer = get(owner, id);
        apply(customer, request);
        return customers.save(customer);
    }

    public void delete(GoogleAccount owner, Long id) {
        Customer customer = get(owner, id);
        if (!tasks.findByCustomerIdAndOwnerIdOrderByScheduledDateDescScheduledTimeDesc(id, owner.getId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer has scheduled tasks; remove or reassign them first");
        }
        customers.delete(customer);
    }

    private void apply(Customer customer, CustomerRequest request) {
        customer.setName(request.name().trim());
        customer.setPhone(request.phone());
        customer.setEmail(request.email());
        customer.setAddress(request.address());
        customer.setNotes(request.notes());
        customer.setEntryPassword(request.entryPassword());
    }
}
