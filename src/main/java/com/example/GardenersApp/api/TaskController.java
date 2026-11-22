package com.example.GardenersApp.api;

import com.example.GardenersApp.domain.GoogleAccount;
import com.example.GardenersApp.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService service;

    public TaskController(TaskService s) {
        service = s;
    }

    @GetMapping
    public List<TaskResponse> list(@RequestAttribute("authenticatedGoogleAccount") GoogleAccount owner, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @RequestParam(required = false) Long customerId) {
        return service.list(owner, date, customerId).stream().map(TaskResponse::from).toList();
    }

    @GetMapping("/{id}")
    public TaskResponse get(@RequestAttribute("authenticatedGoogleAccount") GoogleAccount owner, @PathVariable Long id) {
        return TaskResponse.from(service.get(owner, id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@RequestAttribute("authenticatedGoogleAccount") GoogleAccount owner, @Valid @RequestBody TaskRequest r) {
        return TaskResponse.from(service.create(owner, r));
    }

    @PutMapping("/{id}")
    public TaskResponse update(@RequestAttribute("authenticatedGoogleAccount") GoogleAccount owner, @PathVariable Long id, @Valid @RequestBody TaskRequest r) {
        return TaskResponse.from(service.update(owner, id, r));
    }

    @PatchMapping("/{id}/completion")
    public TaskResponse completion(@RequestAttribute("authenticatedGoogleAccount") GoogleAccount owner, @PathVariable Long id, @RequestParam boolean completed) {
        return TaskResponse.from(service.completion(owner, id, completed));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestAttribute("authenticatedGoogleAccount") GoogleAccount owner, @PathVariable Long id) {
        service.delete(owner, id);
    }
}
