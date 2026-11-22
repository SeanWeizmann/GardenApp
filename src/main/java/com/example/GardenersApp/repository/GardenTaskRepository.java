package com.example.GardenersApp.repository;

import com.example.GardenersApp.domain.GardenTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface GardenTaskRepository extends JpaRepository<GardenTask, Long> {
    List<GardenTask> findAllByOwnerIdOrderByScheduledDateAscScheduledTimeAsc(Long ownerId);

    List<GardenTask> findByOwnerIdAndScheduledDateOrderByScheduledTimeAsc(Long ownerId, LocalDate date);

    List<GardenTask> findByCustomerIdAndOwnerIdOrderByScheduledDateDescScheduledTimeDesc(Long customerId, Long ownerId);

    java.util.Optional<GardenTask> findByIdAndOwnerId(Long id, Long ownerId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("update GardenTask t set t.owner = :owner where t.owner is null")
    int claimUnowned(@org.springframework.data.repository.query.Param("owner") com.example.GardenersApp.domain.GoogleAccount owner);
}
