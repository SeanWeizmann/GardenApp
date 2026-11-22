package com.example.GardenersApp.repository;

import com.example.GardenersApp.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByIdAndOwnerId(Long id, Long ownerId);

    List<Customer> findAllByOwnerIdOrderByNameAsc(Long ownerId);

    Optional<Customer> findFirstByOwnerIdAndNameIgnoreCase(Long ownerId, String name);

    @Query("select c from Customer c where c.owner.id = :ownerId and (lower(c.name) like lower(concat('%', :q, '%')) or lower(coalesce(c.phone,'')) like lower(concat('%', :q, '%')) or lower(coalesce(c.email,'')) like lower(concat('%', :q, '%'))) order by c.name")
    List<Customer> search(@Param("ownerId") Long ownerId, @Param("q") String q);

    @org.springframework.data.jpa.repository.Modifying
    @Query("update Customer c set c.owner = :owner where c.owner is null")
    int claimUnowned(@Param("owner") com.example.GardenersApp.domain.GoogleAccount owner);
}
