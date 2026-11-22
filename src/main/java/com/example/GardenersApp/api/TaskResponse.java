package com.example.GardenersApp.api;

import com.example.GardenersApp.domain.GardenTask;

public record TaskResponse(Long id, String title, Long customerId, Long clientId, String client, String date,
                           String time,
                           int durationMinutes, String address, String entranceCode, String notes, String status,
                           String priority,
                           boolean isCompleted, String assignee) {
    public static TaskResponse from(GardenTask t) {
        Long linkedClientId = t.getCustomer() == null ? null : t.getCustomer().getId();
        return new TaskResponse(t.getId(), t.getTitle(), linkedClientId, linkedClientId, t.getCustomerName(), t.getScheduledDate().toString(), t.getScheduledTime().toString(), t.getDurationMinutes(), t.getAddress(), t.getEntranceCode(), t.getNotes(), t.getStatus().name(), t.getPriority().name(), t.getStatus() == GardenTask.Status.COMPLETED, t.getAssignee());
    }
}
