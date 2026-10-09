package com.eventmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="registrations", uniqueConstraints=@UniqueConstraint(columnNames={"event_id","user_id"}))
public class Registration {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional=false) @JoinColumn(name="event_id")
    private Event event;
    @Column(name="user_id") private Long userId;
    @Column(name="organizer_id") private Long organizerId;
    private String fullName;
    private String email;
    private String phone;
    private LocalDateTime registeredAt = LocalDateTime.now();
    private String status = "CONFIRMED";

    public Registration() {}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Event getEvent(){return event;} public void setEvent(Event event){this.event=event;}
    public Long getUserId(){return userId;} public void setUserId(Long userId){this.userId=userId;}
    public Long getOrganizerId(){return organizerId;} public void setOrganizerId(Long organizerId){this.organizerId=organizerId;}
    public String getFullName(){return fullName;} public void setFullName(String fullName){this.fullName=fullName;}
    public String getEmail(){return email;} public void setEmail(String email){this.email=email;}
    public String getPhone(){return phone;} public void setPhone(String phone){this.phone=phone;}
    public LocalDateTime getRegisteredAt(){return registeredAt;} public void setRegisteredAt(LocalDateTime registeredAt){this.registeredAt=registeredAt;}
    public String getStatus(){return status;} public void setStatus(String status){this.status=status;}
}
