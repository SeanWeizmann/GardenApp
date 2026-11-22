package com.example.GardenersApp.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Wire format used by the Android MissionsApiService.
 */
public record MissionRequest(
        String title,
        String client,
        String date,
        String time,
        String address,
        String notes,
        String status,
        String priority,
        @JsonProperty("is_completed") Boolean isCompleted,
        String assignee,
        @JsonProperty("assignee_initial") String assigneeInitial,
        @JsonProperty("entrance_code") String entranceCode) {
}
