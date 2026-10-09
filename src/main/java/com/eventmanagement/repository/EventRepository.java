package com.eventmanagement.repository;
import com.eventmanagement.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByCategoryIgnoreCase(String category);
    List<Event> findByOrganizerId(Long organizerId);
}
