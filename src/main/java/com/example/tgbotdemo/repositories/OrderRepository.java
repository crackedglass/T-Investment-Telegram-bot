package com.example.tgbotdemo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.tgbotdemo.domain.*;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o JOIN o.user JOIN o.cell WHERE o.user.username = :name")
    public List<Order> findByUsername(@Param("name") String username);

    @Query("SELECT o FROM Order o RIGHT JOIN o.user RIGHT JOIN o.cell WHERE o.user=:userToFind AND o.cell=:cellToFind")
    public Order findByUserAndCell(User userToFind, Cell cellToFind);
}
