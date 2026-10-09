package com.eventmanagement.controller;

import com.eventmanagement.entity.Event;
import com.eventmanagement.entity.User;
import com.eventmanagement.repository.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@CrossOrigin
public class EventController {
    private final EventRepository events;
    private final RegistrationRepository registrations;
    private final UserRepository users;
    public EventController(EventRepository events, RegistrationRepository registrations, UserRepository users){
        this.events=events; this.registrations=registrations; this.users=users;
    }

    @GetMapping
    public List<Event> all(@RequestParam(required=false) String category) {
        return (category == null || category.isBlank()) ? events.findAll() : events.findByCategoryIgnoreCase(category);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> get(@PathVariable Long id) {
        return events.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Event event, HttpSession session) {
        User actor=actor(session);
        if(actor==null || (!"ADMIN".equals(actor.getRole()) && !"ORGANIZER".equals(actor.getRole())))
            return forbidden("Admin or Organizer login required.");
        event.setId(null);
        if("ORGANIZER".equals(actor.getRole())){
            event.setOrganizerId(actor.getId());
            event.setOrganizerUsername(actor.getUsername());
            event.setOrganizerName(actor.getFullName());
        } else {
            if(event.getOrganizerId()!=null){
                User owner=users.findById(event.getOrganizerId()).orElse(null);
                if(owner==null || !"ORGANIZER".equals(owner.getRole())) return bad("A valid organizer is required.");
                event.setOrganizerUsername(owner.getUsername()); event.setOrganizerName(owner.getFullName());
            } else {
                event.setOrganizerId(actor.getId());
                event.setOrganizerUsername(actor.getUsername());
                event.setOrganizerName(actor.getFullName());
            }
        }
        if(event.getStatus()==null || event.getStatus().isBlank()) event.setStatus("OPEN");
        return ResponseEntity.status(201).body(events.save(event));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,@Valid @RequestBody Event input,HttpSession session){
        User actor=actor(session);
        if(actor==null || (!"ADMIN".equals(actor.getRole()) && !"ORGANIZER".equals(actor.getRole()))) return forbidden("Admin or Organizer login required.");
        Event existing=events.findById(id).orElse(null);
        if(existing==null) return ResponseEntity.notFound().build();
        if("ORGANIZER".equals(actor.getRole()) && !Objects.equals(existing.getOrganizerId(),actor.getId()))
            return forbidden("You can manage only your own events.");
        existing.setTitle(input.getTitle()); existing.setDescription(input.getDescription()); existing.setCategory(input.getCategory());
        existing.setEventDate(input.getEventDate()); existing.setEventTime(input.getEventTime()); existing.setVenue(input.getVenue());
        existing.setCapacity(input.getCapacity()); existing.setRegistrationFee(input.getRegistrationFee());
        existing.setStatus(input.getStatus()==null||input.getStatus().isBlank()?"OPEN":input.getStatus());
        if("ADMIN".equals(actor.getRole()) && input.getOrganizerId()!=null){
            User owner=users.findById(input.getOrganizerId()).orElse(null);
            if(owner==null || !"ORGANIZER".equals(owner.getRole())) return bad("A valid organizer is required.");
            existing.setOrganizerId(owner.getId()); existing.setOrganizerUsername(owner.getUsername()); existing.setOrganizerName(owner.getFullName());
        }
        return ResponseEntity.ok(events.save(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,HttpSession session){
        User actor=actor(session);
        if(actor==null || (!"ADMIN".equals(actor.getRole()) && !"ORGANIZER".equals(actor.getRole()))) return forbidden("Admin or Organizer login required.");
        Event e=events.findById(id).orElse(null); if(e==null)return ResponseEntity.notFound().build();
        if("ORGANIZER".equals(actor.getRole())&&!Objects.equals(e.getOrganizerId(),actor.getId())) return forbidden("You can manage only your own events.");
        registrations.deleteAll(registrations.findByEventId(id)); events.deleteById(id); return ResponseEntity.noContent().build();
    }

    @GetMapping("/manage")
    public ResponseEntity<?> manage(HttpSession session){
        User actor=actor(session);
        if(actor==null || (!"ADMIN".equals(actor.getRole()) && !"ORGANIZER".equals(actor.getRole()))) return forbidden("Admin or Organizer login required.");
        return ResponseEntity.ok("ORGANIZER".equals(actor.getRole()) ? events.findByOrganizerId(actor.getId()) : events.findAll());
    }

    @GetMapping("/{id}/registration-count")
    public Map<String,Long> count(@PathVariable Long id){ return Map.of("count",registrations.countByEventIdAndStatus(id,"CONFIRMED")); }

    private User actor(HttpSession s){ Object id=s.getAttribute("userId"); return id instanceof Long ? users.findById((Long)id).orElse(null):null; }
    private ResponseEntity<Map<String,String>> forbidden(String m){return ResponseEntity.status(403).body(Map.of("message",m));}
    private ResponseEntity<Map<String,String>> bad(String m){return ResponseEntity.badRequest().body(Map.of("message",m));}
}
