package com.example.GardenersApp.api;

import com.example.GardenersApp.domain.Customer;

public record CustomerResponse(Long id, String name, String phone, String email, String address, String notes,
                               String entryPassword) {
    public static CustomerResponse from(Customer c) {
        return new CustomerResponse(c.getId(), c.getName(), c.getPhone(), c.getEmail(), c.getAddress(), c.getNotes(),
                c.getEntryPassword());
    }
}
