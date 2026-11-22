package com.example.GardenersApp.api;

import com.example.GardenersApp.domain.GardenTask;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskRequest(@NotBlank @Size(max = 160) String title, @JsonAlias("customerId") Long clientId,
                          @NotBlank @Size(max = 120) String client, @NotBlank String date, @NotBlank String time,
                          @Min(15) @Max(1440) Integer durationMinutes, @Size(max = 300) String address,
                          @Size(max = 80) String entranceCode, @Size(max = 2000) String notes,
                          GardenTask.Status status, GardenTask.Priority priority, @Size(max = 120) String assignee) {
}
