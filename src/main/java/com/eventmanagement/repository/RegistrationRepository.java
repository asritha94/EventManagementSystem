package com.eventmanagement.repository;
import com.eventmanagement.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByEventId(Long eventId);
    List<Registration> findByUserId(Long userId);
    List<Registration> findByOrganizerId(Long organizerId);
    List<Registration> findByEmailIgnoreCase(String email);
    boolean existsByEventIdAndUserId(Long eventId, Long userId);
    long countByEventIdAndStatus(Long eventId, String status);
}
