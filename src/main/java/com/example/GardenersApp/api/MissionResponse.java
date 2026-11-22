package com.example.GardenersApp.api;

import com.example.GardenersApp.domain.GardenTask;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.format.DateTimeFormatter;

/**
 * Mission JSON names and localized values expected by the Android client.
 */
public record MissionResponse(int id, String title, String client, String date, String time,
                              String address, String notes, String status, String priority,
                              @JsonProperty("is_completed") boolean isCompleted, String assignee,
                              @JsonProperty("assignee_initial") String assigneeInitial,
                              @JsonProperty("entrance_code") String entranceCode) {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    public static MissionResponse from(GardenTask task) {
        boolean completed = task.getStatus() == GardenTask.Status.COMPLETED;
        return new MissionResponse(Math.toIntExact(task.getId()), task.getTitle(), task.getCustomerName(),
                task.getScheduledDate().format(DATE), task.getScheduledTime().format(TIME),
                task.getAddress(), task.getNotes(), status(task.getStatus()), priority(task.getPriority()),
                completed, task.getAssignee(), initial(task.getAssignee()), task.getEntranceCode());
    }

    private static String status(GardenTask.Status status) {
        return switch (status) {
            case NEW -> "חדש";
            case OPEN -> "פתוח";
            case IN_PROGRESS -> "בתהליך";
            case COMPLETED -> "הושלם";
            case CANCELLED -> "בוטל";
        };
    }

    private static String priority(GardenTask.Priority priority) {
        return switch (priority) {
            case LOW -> "נמוכה";
            case MEDIUM -> "בינונית";
            case HIGH -> "גבוהה";
        };
    }

    private static String initial(String assignee) {
        return assignee == null || assignee.isBlank() ? "" : assignee.substring(0, 1);
    }
}
