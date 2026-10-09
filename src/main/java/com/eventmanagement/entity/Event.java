package com.eventmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "events")
public class Event {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank private String title;
    @Column(length = 2000) private String description;
    @NotBlank private String category;
    @NotNull private LocalDate eventDate;
    private LocalTime eventTime;
    @NotBlank private String venue;
    @Min(1) private int capacity;
    @Min(0) private double registrationFee;
    @NotBlank private String organizerName;
    @Column(name = "organizer_username") private String organizerUsername;
    @Column(name = "organizer_id") private Long organizerId;
    private String status = "OPEN";

    public Event() {}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getTitle(){return title;} public void setTitle(String title){this.title=title;}
    public String getDescription(){return description;} public void setDescription(String description){this.description=description;}
    public String getCategory(){return category;} public void setCategory(String category){this.category=category;}
    public LocalDate getEventDate(){return eventDate;} public void setEventDate(LocalDate eventDate){this.eventDate=eventDate;}
    public LocalTime getEventTime(){return eventTime;} public void setEventTime(LocalTime eventTime){this.eventTime=eventTime;}
    public String getVenue(){return venue;} public void setVenue(String venue){this.venue=venue;}
    public int getCapacity(){return capacity;} public void setCapacity(int capacity){this.capacity=capacity;}
    public double getRegistrationFee(){return registrationFee;} public void setRegistrationFee(double registrationFee){this.registrationFee=registrationFee;}
    public String getOrganizerName(){return organizerName;} public void setOrganizerName(String organizerName){this.organizerName=organizerName;}
    public String getOrganizerUsername(){return organizerUsername;} public void setOrganizerUsername(String organizerUsername){this.organizerUsername=organizerUsername;}
    public Long getOrganizerId(){return organizerId;} public void setOrganizerId(Long organizerId){this.organizerId=organizerId;}
    public String getStatus(){return status;} public void setStatus(String status){this.status=status;}
}
