package com.eventmanagement.controller;

import com.eventmanagement.entity.*;
import com.eventmanagement.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/registrations")
@CrossOrigin
public class RegistrationController {
    private final RegistrationRepository registrations;
    private final EventRepository events;
    private final UserRepository users;
    public RegistrationController(RegistrationRepository registrations,EventRepository events,UserRepository users){
        this.registrations=registrations;this.events=events;this.users=users;
    }

    @PostMapping
    public ResponseEntity<?> register(@RequestBody Map<String,String> body,HttpSession session){
        User user=current(session);
        if(user==null) return forbidden("Please login to continue.");
        if(!"USER".equals(user.getRole())) return forbidden("Only normal users can register for events.");
        try{
            Long eventId=Long.valueOf(body.get("eventId"));
            Event event=events.findById(eventId).orElse(null);
            if(event==null)return ResponseEntity.status(404).body(Map.of("message","Event not found."));
            if(registrations.existsByEventIdAndUserId(eventId,user.getId()))
                return ResponseEntity.badRequest().body(Map.of("message","You are already registered for this event."));
            if(!"OPEN".equalsIgnoreCase(event.getStatus()))return ResponseEntity.badRequest().body(Map.of("message","Registration is not open for this event."));
            if(registrations.countByEventIdAndStatus(eventId,"CONFIRMED")>=event.getCapacity())return ResponseEntity.badRequest().body(Map.of("message","This event is full."));
            Registration r=new Registration(); r.setEvent(event); r.setUserId(user.getId()); r.setOrganizerId(event.getOrganizerId());
            r.setFullName(user.getFullName()); r.setEmail(user.getEmail()); r.setPhone(body.getOrDefault("phone",""));
            return ResponseEntity.status(201).body(registrations.save(r));
        }catch(Exception ex){return ResponseEntity.badRequest().body(Map.of("message","Please provide a valid event."));}
    }

    @GetMapping("/my")
    public ResponseEntity<?> mine(HttpSession session){
        User user=current(session);
        if(user==null)return forbidden("Please login to continue.");
        if(!"USER".equals(user.getRole()))return forbidden("User login required.");
        return ResponseEntity.ok(registrations.findByUserId(user.getId()));
    }

    @GetMapping
    public ResponseEntity<?> all(HttpSession session){
        User user=current(session);
        if(user==null||!"ADMIN".equals(user.getRole()))return forbidden("Admin login required.");
        return ResponseEntity.ok(registrations.findAll());
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> byEvent(@PathVariable Long eventId,HttpSession session){
        User user=current(session);
        if(user==null)return forbidden("Please login to continue.");
        Event e=events.findById(eventId).orElse(null);
        if(e==null)return ResponseEntity.notFound().build();
        if("ADMIN".equals(user.getRole()))return ResponseEntity.ok(registrations.findByEventId(eventId));
        if("ORGANIZER".equals(user.getRole())&&Objects.equals(e.getOrganizerId(),user.getId()))
            return ResponseEntity.ok(registrations.findByEventId(eventId));
        return forbidden("You can view registrations only for your own events.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancel(@PathVariable Long id,HttpSession session){
        User user=current(session);
        if(user==null)return forbidden("Please login to continue.");
        Registration r=registrations.findById(id).orElse(null);
        if(r==null)return ResponseEntity.notFound().build();
        boolean allowed="ADMIN".equals(user.getRole())
            || ("USER".equals(user.getRole())&&Objects.equals(r.getUserId(),user.getId()))
            || ("ORGANIZER".equals(user.getRole())&&Objects.equals(r.getOrganizerId(),user.getId()));
        if(!allowed)return forbidden("You are not authorized to perform this action.");
        registrations.deleteById(id);return ResponseEntity.noContent().build();
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> legacyEmailLookup(@PathVariable String email,HttpSession session){
        User user=current(session);
        if(user==null)return forbidden("Please login to continue.");
        if("ADMIN".equals(user.getRole()))return ResponseEntity.ok(registrations.findByEmailIgnoreCase(email));
        if("USER".equals(user.getRole())&&user.getEmail().equalsIgnoreCase(email))return ResponseEntity.ok(registrations.findByUserId(user.getId()));
        return forbidden("You are not authorized to access this information.");
    }

    private User current(HttpSession s){Object id=s.getAttribute("userId");return id instanceof Long?users.findById((Long)id).orElse(null):null;}
    private ResponseEntity<Map<String,String>> forbidden(String m){return ResponseEntity.status(403).body(Map.of("message",m));}
}
