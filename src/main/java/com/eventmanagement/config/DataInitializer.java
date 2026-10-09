package com.eventmanagement.config;

import com.eventmanagement.entity.*;
import com.eventmanagement.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean public BCryptPasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}

    @Bean
    CommandLineRunner seedUsers(UserRepository users,EventRepository events,RegistrationRepository registrations,BCryptPasswordEncoder encoder){
        return args->{
            User admin=createIfMissing(users,encoder,"admin","admin123","System Admin","admin@eventease.com","ADMIN");
            enforceSingleAdmin(users, admin);
            repairUserStatuses(users);
            User o1=createIfMissing(users,encoder,"organizer1","organizer123","Organizer One","organizer1@eventease.com","ORGANIZER");
            User u1=createIfMissing(users,encoder,"user1","user123","User One","user1@eventease.com","USER");
            // Demo environment intentionally seeds exactly one account for each role.
            // Additional ORGANIZER accounts are created by ADMIN; additional USER accounts can register publicly.
            migrateEvents(events,users,o1,o1);
            migrateRegistrations(registrations,users,events);
        };
    }

    private void repairUserStatuses(UserRepository users){
        var all=users.findAll(); boolean changed=false;
        for(User u:all){ if(u.getStatus()==null||u.getStatus().isBlank()){u.setStatus("ACTIVE");changed=true;} }
        if(changed)users.saveAll(all);
    }

    private void enforceSingleAdmin(UserRepository users, User admin){
        for(User u: users.findByRoleIgnoreCase("ADMIN")){
            if(!u.getId().equals(admin.getId())){ u.setRole("USER"); users.save(u); }
        }
        if(!"ADMIN".equals(admin.getRole())){ admin.setRole("ADMIN"); users.save(admin); }
    }

    private User createIfMissing(UserRepository users,BCryptPasswordEncoder enc,String un,String pw,String name,String email,String role){
        return users.findByUsernameIgnoreCase(un).orElseGet(()->users.save(new User(un,enc.encode(pw),name,email,role)));
    }

    private void migrateEvents(EventRepository events,UserRepository users,User preferred,User legacy){
        var all=events.findAll(); boolean changed=false;
        for(Event e:all){
            if(e.getOrganizerId()==null){
                User owner=null;
                if(e.getOrganizerUsername()!=null) owner=users.findByUsernameIgnoreCase(e.getOrganizerUsername()).orElse(null);
                if(owner==null && e.getOrganizerName()!=null){
                    String n=e.getOrganizerName().trim();
                    if(n.equalsIgnoreCase(legacy.getFullName())||n.equalsIgnoreCase(legacy.getUsername())) owner=legacy;
                    else if(n.equalsIgnoreCase(preferred.getFullName())||n.equalsIgnoreCase(preferred.getUsername())) owner=preferred;
                }
                if(owner==null) owner=legacy;
                e.setOrganizerId(owner.getId());e.setOrganizerUsername(owner.getUsername());e.setOrganizerName(owner.getFullName());changed=true;
            }
        }
        if(changed)events.saveAll(all);
    }

    private void migrateRegistrations(RegistrationRepository regs,UserRepository users,EventRepository events){
        var all=regs.findAll(); boolean changed=false;
        for(Registration r:all){
            if(r.getUserId()==null && r.getEmail()!=null){
                User u=users.findAll().stream().filter(x->x.getEmail().equalsIgnoreCase(r.getEmail())).findFirst().orElse(null);
                if(u!=null){r.setUserId(u.getId()); if(r.getEvent()!=null)r.setOrganizerId(r.getEvent().getOrganizerId()); changed=true;}
            }
            if(r.getOrganizerId()==null&&r.getEvent()!=null&&r.getEvent().getOrganizerId()!=null){r.setOrganizerId(r.getEvent().getOrganizerId());changed=true;}
        }
        if(changed)regs.saveAll(all);
    }
}
