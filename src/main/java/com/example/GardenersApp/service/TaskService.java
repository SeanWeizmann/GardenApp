package com.example.GardenersApp.service;

import com.example.GardenersApp.api.TaskRequest;
import com.example.GardenersApp.domain.Customer;
import com.example.GardenersApp.domain.GardenTask;
import com.example.GardenersApp.domain.GoogleAccount;
import com.example.GardenersApp.repository.CustomerRepository;
import com.example.GardenersApp.repository.GardenTaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@Transactional
public class TaskService {
    private final GardenTaskRepository tasks;
    private final CustomerRepository customers;

    public TaskService(GardenTaskRepository tasks, CustomerRepository customers) {
        this.tasks = tasks;
        this.customers = customers;
    }

    public List<GardenTask> list(GoogleAccount owner, LocalDate date, Long customerId) {
        if (customerId != null) {
            return tasks.findByCustomerIdAndOwnerIdOrderByScheduledDateDescScheduledTimeDesc(customerId, owner.getId());
        }
        return date == null
                ? tasks.findAllByOwnerIdOrderByScheduledDateAscScheduledTimeAsc(owner.getId())
                : tasks.findByOwnerIdAndScheduledDateOrderByScheduledTimeAsc(owner.getId(), date);
    }

    public GardenTask get(GoogleAccount owner, Long id) {
        return tasks.findByIdAndOwnerId(id, owner.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    public GardenTask create(GoogleAccount owner, TaskRequest request) {
        GardenTask task = new GardenTask();
        task.setOwner(owner);
        apply(task, owner, request);
        return tasks.save(task);
    }

    public GardenTask update(GoogleAccount owner, Long id, TaskRequest request) {
        GardenTask task = get(owner, id);
        apply(task, owner, request);
        return tasks.save(task);
    }

    public GardenTask completion(GoogleAccount owner, Long id, boolean completed) {
        GardenTask task = get(owner, id);
        task.setStatus(completed ? GardenTask.Status.COMPLETED : GardenTask.Status.OPEN);
        return tasks.save(task);
    }

    public void delete(GoogleAccount owner, Long id) {
        tasks.delete(get(owner, id));
    }

    private void apply(GardenTask task, GoogleAccount owner, TaskRequest request) {
        try {
            task.setScheduledDate(LocalDate.parse(request.date()));
            task.setScheduledTime(LocalTime.parse(request.time()));
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date must be yyyy-MM-dd and time must be HH:mm");
        }
        task.setOwner(owner);
        task.setTitle(request.title().trim());
        task.setCustomerName(request.client().trim());
        Customer customer = request.clientId() == null ? null : customers.findByIdAndOwnerId(request.clientId(), owner.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        task.setCustomer(customer);
        task.setDurationMinutes(request.durationMinutes() == null ? 60 : request.durationMinutes());
        task.setAddress(request.address());
        task.setEntranceCode(request.entranceCode());
        task.setNotes(request.notes());
        task.setAssignee(request.assignee() == null ? "" : request.assignee());
        if (request.status() != null) task.setStatus(request.status());
        if (request.priority() != null) task.setPriority(request.priority());
    }

}
