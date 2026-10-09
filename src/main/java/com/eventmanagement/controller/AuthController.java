package com.eventmanagement.controller;

import com.eventmanagement.entity.User;
import com.eventmanagement.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {
    private final UserRepository users;
    private final BCryptPasswordEncoder encoder;
    public AuthController(UserRepository users, BCryptPasswordEncoder encoder){this.users=users;this.encoder=encoder;}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> body, HttpSession session){
        String username=body.getOrDefault("username","").trim();
        String password=body.getOrDefault("password","");
        User user=users.findByUsernameIgnoreCase(username).orElse(null);
        if(user==null || !encoder.matches(password,user.getPassword()))
            return ResponseEntity.status(401).body(Map.of("message","Invalid username or password."));
        if(!"ACTIVE".equalsIgnoreCase(user.getStatus()))
            return ResponseEntity.status(403).body(Map.of("message","This account is inactive."));
        session.setAttribute("userId",user.getId());
        session.setAttribute("username",user.getUsername());
        session.setAttribute("fullName",user.getFullName());
        session.setAttribute("email",user.getEmail());
        session.setAttribute("role",user.getRole());
        return ResponseEntity.ok(publicUser(user));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String,String> body){
        String username=body.getOrDefault("username","").trim();
        String password=body.getOrDefault("password","");
        String fullName=body.getOrDefault("fullName","").trim();
        String email=body.getOrDefault("email","").trim().toLowerCase();
        if(username.isBlank() || password.length()<6 || fullName.isBlank() || email.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message","Name, username, email and a password of at least 6 characters are required."));
        if(users.existsByUsernameIgnoreCase(username)) return ResponseEntity.badRequest().body(Map.of("message","Username already exists."));
        if(users.existsByEmailIgnoreCase(email)) return ResponseEntity.badRequest().body(Map.of("message","Email already exists."));
        User u=new User(username,encoder.encode(password),fullName,email,"USER");
        return ResponseEntity.status(201).body(publicUser(users.save(u)));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session){
        if(session.getAttribute("userId")==null) return ResponseEntity.status(401).body(Map.of("message","Please login to continue."));
        User u=users.findById((Long)session.getAttribute("userId")).orElse(null);
        return u==null ? ResponseEntity.status(401).body(Map.of("message","Please login to continue.")) : ResponseEntity.ok(publicUser(u));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> profile(@RequestBody Map<String,String> body, HttpSession session){
        Long id=(Long)session.getAttribute("userId");
        if(id==null) return ResponseEntity.status(401).body(Map.of("message","Please login to continue."));
        User u=users.findById(id).orElse(null);
        if(u==null) return ResponseEntity.status(401).body(Map.of("message","Please login to continue."));
        String name=body.getOrDefault("fullName","").trim(), email=body.getOrDefault("email","").trim().toLowerCase();
        if(name.isBlank()||email.isBlank()) return ResponseEntity.badRequest().body(Map.of("message","Name and email are required."));
        if(!email.equalsIgnoreCase(u.getEmail()) && users.existsByEmailIgnoreCase(email)) return ResponseEntity.badRequest().body(Map.of("message","Email already exists."));
        u.setFullName(name); u.setEmail(email); users.save(u);
        session.setAttribute("fullName",u.getFullName()); session.setAttribute("email",u.getEmail());
        return ResponseEntity.ok(publicUser(u));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session){ session.invalidate(); return ResponseEntity.ok(Map.of("message","Logged out.")); }

    public static Map<String,Object> publicUser(User u){
        return Map.of("id",u.getId(),"username",u.getUsername(),"fullName",u.getFullName(),"email",u.getEmail(),"role",u.getRole(),"status",u.getStatus());
    }
}
