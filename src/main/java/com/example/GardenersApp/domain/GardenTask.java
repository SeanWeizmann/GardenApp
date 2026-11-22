package com.example.GardenersApp.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "garden_tasks", indexes = {
        @Index(name = "idx_task_schedule", columnList = "scheduled_date,scheduled_time"),
        @Index(name = "idx_task_customer", columnList = "customer_id"),
        @Index(name = "idx_task_owner", columnList = "owner_id")
})
public class GardenTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private GoogleAccount owner;
    @Column(nullable = false, length = 160)
    private String title;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;
    @Column(name = "customer_name", nullable = false, length = 120)
    private String customerName;
    @Column(nullable = false)
    private LocalDate scheduledDate;
    @Column(nullable = false)
    private LocalTime scheduledTime;
    @Column(nullable = false)
    private Integer durationMinutes = 60;
    @Column(length = 300)
    private String address;
    @Column(length = 80)
    private String entranceCode;
    @Column(length = 2000)
    private String notes;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.NEW;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority = Priority.MEDIUM;
    @Column(length = 120)
    private String assignee = "";
    @Column(nullable = false)
    private final Instant createdAt = Instant.now();
    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public GoogleAccount getOwner() {
        return owner;
    }

    public void setOwner(GoogleAccount owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String v) {
        title = v;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer v) {
        customer = v;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String v) {
        customerName = v;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate v) {
        scheduledDate = v;
    }

    public LocalTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalTime v) {
        scheduledTime = v;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer v) {
        durationMinutes = v;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String v) {
        address = v;
    }

    public String getEntranceCode() {
        return entranceCode;
    }

    public void setEntranceCode(String v) {
        entranceCode = v;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String v) {
        notes = v;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status v) {
        status = v;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority v) {
        priority = v;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String v) {
        assignee = v;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public enum Status {NEW, OPEN, IN_PROGRESS, COMPLETED, CANCELLED}

    public enum Priority {LOW, MEDIUM, HIGH}
}
