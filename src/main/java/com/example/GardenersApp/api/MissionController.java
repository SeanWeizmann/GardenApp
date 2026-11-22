package com.example.GardenersApp.api;

import com.example.GardenersApp.domain.GardenTask;
import com.example.GardenersApp.domain.GoogleAccount;
import com.example.GardenersApp.repository.CustomerRepository;
import com.example.GardenersApp.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/missions")
public class MissionController {
    private static final DateTimeFormatter CLIENT_DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private final TaskService service;
    private final CustomerRepository customers;

    public MissionController(TaskService service, CustomerRepository customers) {
        this.service = service;
        this.customers = customers;
    }

    @GetMapping
    public List<MissionResponse> list(@RequestAttribute("authenticatedGoogleAccount") GoogleAccount owner) {
        return service.list(owner, null, null).stream().map(MissionResponse::from).toList();
    }

    @GetMapping("/{id}")
    public MissionResponse get(@RequestAttribute("authenticatedGoogleAccount") GoogleAccount owner, @PathVariable Long id) {
        return MissionResponse.from(service.get(owner, id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MissionResponse create(@RequestAttribute("authenticatedGoogleAccount") GoogleAccount owner, @RequestBody MissionRequest request) {
        return MissionResponse.from(service.create(owner, toTaskRequest(owner, request)));
    }

    @PutMapping("/{id}")
    public MissionResponse update(@RequestAttribute("authenticatedGoogleAccount") GoogleAccount owner, @PathVariable Long id, @RequestBody MissionRequest request) {
        return MissionResponse.from(service.update(owner, id, toTaskRequest(owner, request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestAttribute("authenticatedGoogleAccount") GoogleAccount owner, @PathVariable Long id) {
        service.delete(owner, id);
    }

    private TaskRequest toTaskRequest(GoogleAccount owner, MissionRequest r) {
        if (r == null || blank(r.title()) || blank(r.client()) || blank(r.date())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title, client and date are required");
        }
        LocalDate date;
        try {
            date = r.date().contains("/") ? LocalDate.parse(r.date(), CLIENT_DATE) : LocalDate.parse(r.date());
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date must be dd/MM/yyyy");
        }
        String time = blank(r.time()) ? "08:00" : r.time();
        try {
            LocalTime.parse(time);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "time must be HH:mm");
        }
        GardenTask.Status status = r.isCompleted() != null && r.isCompleted()
                ? GardenTask.Status.COMPLETED : parseStatus(r.status());
        if (status == GardenTask.Status.COMPLETED && Boolean.FALSE.equals(r.isCompleted())) {
            status = GardenTask.Status.OPEN;
        }
        Long customerId = customers.findFirstByOwnerIdAndNameIgnoreCase(owner.getId(), r.client().trim()).map(c -> c.getId()).orElse(null);
        return new TaskRequest(r.title(), customerId, r.client(), date.toString(), time, 60,
                r.address(), r.entranceCode(), r.notes(), status, parsePriority(r.priority()), r.assignee());
    }

    private GardenTask.Status parseStatus(String status) {
        if (blank(status)) return GardenTask.Status.NEW;
        return switch (status) {
            case "חדש", "NEW" -> GardenTask.Status.NEW;
            case "פתוח", "OPEN" -> GardenTask.Status.OPEN;
            case "בתהליך", "IN_PROGRESS" -> GardenTask.Status.IN_PROGRESS;
            case "הושלם", "COMPLETED" -> GardenTask.Status.COMPLETED;
            case "בוטל", "CANCELLED" -> GardenTask.Status.CANCELLED;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid mission status");
        };
    }

    private GardenTask.Priority parsePriority(String priority) {
        if (blank(priority)) return GardenTask.Priority.MEDIUM;
        return switch (priority) {
            case "נמוכה", "LOW" -> GardenTask.Priority.LOW;
            case "בינונית", "MEDIUM" -> GardenTask.Priority.MEDIUM;
            case "גבוהה", "HIGH" -> GardenTask.Priority.HIGH;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid mission priority");
        };
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
