package com.eventmanagement.controller;

import com.eventmanagement.entity.*;
import com.eventmanagement.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {
    private final UserRepository users;
    private final EventRepository events;
    private final RegistrationRepository registrations;
    private final BCryptPasswordEncoder encoder;

    public AdminController(UserRepository users,EventRepository events,RegistrationRepository registrations,BCryptPasswordEncoder encoder){
        this.users=users;this.events=events;this.registrations=registrations;this.encoder=encoder;
    }

    @GetMapping("/users")
    public ResponseEntity<?> users(HttpSession s){return admin(s)?ResponseEntity.ok(users.findByRoleIgnoreCase("USER")):forbidden();}
    @GetMapping("/organizers")
    public ResponseEntity<?> organizers(HttpSession s){return admin(s)?ResponseEntity.ok(users.findByRoleIgnoreCase("ORGANIZER")):forbidden();}

    @PostMapping("/organizers")
    public ResponseEntity<?> createOrganizer(@RequestBody Map<String,String> b,HttpSession s){
        if(!admin(s))return forbidden();
        String username=b.getOrDefault("username","").trim(),password=b.getOrDefault("password",""),name=b.getOrDefault("fullName","").trim(),email=b.getOrDefault("email","").trim().toLowerCase();
        if(username.isBlank()||password.length()<6||name.isBlank()||email.isBlank())return bad("Name, username, email and a password of at least 6 characters are required.");
        if(users.existsByUsernameIgnoreCase(username)||users.existsByEmailIgnoreCase(email))return bad("Username or email already exists.");
        User u=new User(username,encoder.encode(password),name,email,"ORGANIZER");return ResponseEntity.status(201).body(AuthController.publicUser(users.save(u)));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> userStatus(@PathVariable Long id,@RequestBody Map<String,String> b,HttpSession s){
        if(!admin(s))return forbidden(); User u=users.findById(id).orElse(null);
        if(u==null||"ADMIN".equals(u.getRole()))return ResponseEntity.notFound().build();
        u.setStatus("ACTIVE".equalsIgnoreCase(b.get("status"))?"ACTIVE":"INACTIVE"); return ResponseEntity.ok(AuthController.publicUser(users.save(u)));
    }

    @PutMapping("/organizers/{id}/status")
    public ResponseEntity<?> organizerStatus(@PathVariable Long id,@RequestBody Map<String,String> b,HttpSession s){
        if(!admin(s))return forbidden(); User u=users.findById(id).orElse(null);
        if(u==null||!"ORGANIZER".equals(u.getRole()))return ResponseEntity.notFound().build();
        u.setStatus("ACTIVE".equalsIgnoreCase(b.get("status"))?"ACTIVE":"INACTIVE"); return ResponseEntity.ok(AuthController.publicUser(users.save(u)));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id,HttpSession s){
        if(!admin(s))return forbidden(); User u=users.findById(id).orElse(null);
        if(u==null||"ADMIN".equals(u.getRole()))return ResponseEntity.notFound().build();
        registrations.deleteAll(registrations.findByUserId(id)); users.deleteById(id); return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/organizers/{id}")
    public ResponseEntity<?> deleteOrganizer(@PathVariable Long id,HttpSession s){
        if(!admin(s))return forbidden(); User u=users.findById(id).orElse(null);
        if(u==null||!"ORGANIZER".equals(u.getRole()))return ResponseEntity.notFound().build();
        events.findByOrganizerId(id).forEach(e->{registrations.deleteAll(registrations.findByEventId(e.getId()));events.deleteById(e.getId());});
        users.deleteById(id); return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats(HttpSession s){
        if(!admin(s))return forbidden();
        return ResponseEntity.ok(Map.of("users",users.findByRoleIgnoreCase("USER").size(),"organizers",users.findByRoleIgnoreCase("ORGANIZER").size(),
            "events",events.count(),"registrations",registrations.count(),"upcomingEvents",events.findAll().stream().filter(e->e.getEventDate()!=null&&!e.getEventDate().isBefore(java.time.LocalDate.now())).count()));
    }

    private boolean admin(HttpSession s){return "ADMIN".equals(s.getAttribute("role"))&&s.getAttribute("userId")!=null;}
    private ResponseEntity<Map<String,String>> forbidden(){return ResponseEntity.status(403).body(Map.of("message","Admin login required."));}
    private ResponseEntity<Map<String,String>> bad(String m){return ResponseEntity.badRequest().body(Map.of("message",m));}
}
