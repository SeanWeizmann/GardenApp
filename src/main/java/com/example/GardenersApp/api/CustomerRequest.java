package com.example.GardenersApp.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(@NotBlank @Size(max = 120) String name, @Size(max = 40) String phone,
                              @Email @Size(max = 160) String email, @Size(max = 300) String address,
                              @Size(max = 2000) String notes, @Size(max = 255) String entryPassword) {
}
