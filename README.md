# 🎉 EventEase – Event Management System

**EventEase** is a full-stack web application developed to simplify event creation, event discovery, registration, and administration. Built using Java, Spring Boot, MySQL, HTML, CSS, and JavaScript, the system provides separate functionality for Admin, Organizers, and Users.

The application supports multiple organizers and users, with role-based access control and ownership-based data isolation. Each organizer manages their own events, each user manages their own registrations, and the Admin oversees the entire platform.

## 📌 Project Overview

* **Project Name:** EventEase – Event Management System
* **Project Type:** Full-Stack Web Application
* **Backend:** Java 17, Spring Boot 3.4.3
* **Frontend:** HTML5, CSS3, JavaScript
* **Database:** MySQL
* **ORM:** Spring Data JPA, Hibernate
* **Authentication:** HTTP Sessions
* **Password Security:** BCrypt
* **Build Tool:** Maven

## ✨ Features

### 👨‍💼 1. Admin Dashboard

The Admin has system-wide access to manage the platform.

* View dashboard statistics.
* View and manage user accounts.
* Create and manage multiple organizer accounts.
* Activate or deactivate eligible user and organizer accounts.
* View and manage all events.
* View registrations across the platform.
* Assign events to organizers.
* Maintain centralized control over event management.

### 🧑‍💻 2. Organizer Dashboard

Multiple organizers can independently manage their events.

* Log in using individual organizer credentials.
* Create new events.
* Edit event information.
* Delete events they own.
* View and manage their own events.
* View registrations for their events.
* Track participant counts.
* Manage event details, including title, description, category, date, time, venue, capacity, and registration fee.

Each event is associated with its organizer's unique ID. Organizers cannot modify or delete events owned by other organizers.

### 👤 3. User Dashboard

Users can discover events and manage their registrations.

* Register for a new account.
* Log in and log out.
* Browse available events.
* View event details.
* Register for events that are open and have available capacity.
* View personal registration history.
* Cancel their own registrations.
* Update profile information.

Every user has an independent account, and registration information is associated with the logged-in user.

### 📅 4. Event Management

* Create, read, update, and delete events.
* Maintain event categories and descriptions.
* Store event dates, times, and venues.
* Set registration fees and participant capacity.
* Track event status.
* Associate each event with its organizer.
* Track confirmed registrations.

### 📝 5. Registration Management

* Register users for events.
* Prevent duplicate registrations for the same user and event.
* Prevent registration when an event is closed or full.
* Store registration details and timestamps.
* Allow users to access their own registrations.
* Allow organizers to view registrations for their events.
* Allow Admin to view registrations across the platform.

### 🔐 6. Role-Based Access and Data Isolation

EventEase supports three roles:

| Role      | Responsibilities                                                        |
| --------- | ----------------------------------------------------------------------- |
| ADMIN     | Manage users, organizers, events, and registrations across the platform |
| ORGANIZER | Create and manage owned events and view their registrations             |
| USER      | Browse events and manage personal registrations                         |

The backend verifies authentication, roles, and resource ownership. Frontend restrictions improve usability, while server-side checks protect restricted operations.

## ⚙️ How the Project Works

### Application Workflow

1. **Start the application:** Spring Boot starts the server and connects to the MySQL database.
2. **Initialize data:** The application creates the default demo accounts if they do not already exist and updates supported database schema changes.
3. **Authenticate users:** Users log in with their credentials. The backend verifies the password and establishes an HTTP session.
4. **Identify the role:** The application identifies whether the logged-in account is an Admin, Organizer, or User.
5. **Display the appropriate dashboard:** The frontend provides role-specific navigation and available actions.
6. **Manage events:** Admin or an Organizer creates events. Events created by an Organizer are associated with that organizer's account.
7. **Register for events:** Authenticated Users can register for open events when capacity is available.
8. **Store registration information:** Registration records are saved in MySQL with references to the event, user, and organizer.
9. **Enforce permissions:** The backend checks ownership and role permissions for protected operations.
10. **Maintain separate accounts:** Users see their own registrations, organizers see registrations for their events, and Admin can view platform-wide records.
11. **Log out:** The current HTTP session is invalidated.

### Example: Admin + Two Organizers + Two Users

Suppose the application has one Admin, two Organizers, and two Users.

1. Admin logs in and creates Organizer 2.
2. Organizer 1 creates a Java Workshop.
3. Organizer 2 creates a React Workshop.
4. Organizer 1 manages the Java Workshop, while Organizer 2 manages the React Workshop.
5. If Organizer 2 attempts to modify Organizer 1's event, the backend denies the request.
6. User 1 registers for the Java Workshop.
7. User 2 registers for the React Workshop.
8. User 1 sees only their own registrations, and User 2 sees only theirs.
9. Admin can access the events and registrations across the platform.

This workflow demonstrates how EventEase supports multiple independent organizers and users without mixing their data.

## 🛠️ Technologies Used

| Technology      | Purpose                                 |
| --------------- | --------------------------------------- |
| Java 17         | Core application programming            |
| Spring Boot     | Backend application framework           |
| Spring Web      | REST API development                    |
| Spring Data JPA | Database operations                     |
| Hibernate       | Object-relational mapping               |
| MySQL           | Persistent data storage                 |
| HTML5           | Page structure                          |
| CSS3            | Styling and responsive layout           |
| JavaScript      | Frontend interactions and API calls     |
| BCrypt          | Password hashing                        |
| HTTP Sessions   | Authentication and session management   |
| Maven           | Dependency management and project build |

## 🗂️ Project Structure

```text
EventManagementSystem/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── com/eventmanagement/
        │       ├── EventManagementApplication.java
        │       ├── config/
        │       │   └── DataInitializer.java
        │       ├── controller/
        │       │   ├── AuthController.java
        │       │   ├── EventController.java
        │       │   ├── RegistrationController.java
        │       │   ├── AdminController.java
        │       │   └── ApiExceptionHandler.java
        │       ├── entity/
        │       │   ├── User.java
        │       │   ├── Event.java
        │       │   └── Registration.java
        │       └── repository/
        │           ├── UserRepository.java
        │           ├── EventRepository.java
        │           └── RegistrationRepository.java
        └── resources/
            ├── application.properties
            └── static/
                ├── index.html
                ├── css/
                │   └── style.css
                └── js/
                    └── app.js
```

## 🚀 Installation and Setup

### Prerequisites

* Java Development Kit (JDK) 17 or later.
* MySQL Server.
* Maven.
* Visual Studio Code, IntelliJ IDEA, or Eclipse.
* A modern web browser.

### Step 1: Clone the Repository

```bash
git clone https://github.com/asritha94/EventManagementSystem.git
cd EventManagementSystem
```

Open the directory containing `pom.xml` in your IDE.

### Step 2: Create the Database

Start MySQL and execute:

```sql
CREATE DATABASE event_management_db;
```

### Step 3: Configure Database Credentials

Open `src/main/resources/application.properties` and configure your local MySQL username and password.

Example:

```properties
spring.application.name=event-management-system
spring.datasource.url=jdbc:mysql://localhost:3306/event_management_db?createDatabaseIfNotExist=true
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false
server.port=8080
```

Set the `DB_PASSWORD` environment variable to your MySQL password before starting the application. Do not commit actual credentials to GitHub.

### Step 4: Build the Project

Open a terminal in the directory containing `pom.xml` and execute:

```bash
mvn clean install
```

### Step 5: Run the Application

```bash
mvn spring-boot:run
```

Alternatively, run `EventManagementApplication.java` from your IDE.

### Step 6: Open the Application

Navigate to:

**http://localhost:8080**

Register a new User account or log in using one of the configured demo accounts.

## 🔑 Demo Credentials

The application initializes one default account for each role.

| Role      | Username     | Password       |
| --------- | ------------ | -------------- |
| Admin     | `admin`      | `admin123`     |
| Organizer | `organizer1` | `organizer123` |
| User      | `user1`      | `user123`      |

Additional Organizer accounts can be created by Admin, and additional User accounts can be registered through the public registration form.

These credentials are for local demonstration only. Change them before any public deployment.

## 🔌 Main API Endpoints

### Authentication

| Method | Endpoint             | Description             |
| ------ | -------------------- | ----------------------- |
| POST   | `/api/auth/login`    | Log in                  |
| POST   | `/api/auth/register` | Register a User         |
| GET    | `/api/auth/me`       | Get the current account |
| PUT    | `/api/auth/profile`  | Update profile          |
| POST   | `/api/auth/logout`   | Log out                 |

### Events

| Method | Endpoint                              | Description                      |
| ------ | ------------------------------------- | -------------------------------- |
| GET    | `/api/events`                         | Browse events                    |
| GET    | `/api/events/{id}`                    | View event details               |
| POST   | `/api/events`                         | Create an event                  |
| PUT    | `/api/events/{id}`                    | Update an authorized event       |
| DELETE | `/api/events/{id}`                    | Delete an authorized event       |
| GET    | `/api/events/manage`                  | Retrieve manageable events       |
| GET    | `/api/events/{id}/registration-count` | Get confirmed registration count |

### Registrations

| Method | Endpoint                             | Description                                 |
| ------ | ------------------------------------ | ------------------------------------------- |
| POST   | `/api/registrations`                 | Register for an event                       |
| GET    | `/api/registrations/my`              | View personal registrations                 |
| GET    | `/api/registrations`                 | View all registrations as Admin             |
| GET    | `/api/registrations/event/{eventId}` | View registrations for an authorized event  |
| DELETE | `/api/registrations/{id}`            | Cancel or remove an authorized registration |

### Administration

| Method | Endpoint                            | Description               |
| ------ | ----------------------------------- | ------------------------- |
| GET    | `/api/admin/users`                  | View users                |
| GET    | `/api/admin/organizers`             | View organizers           |
| POST   | `/api/admin/organizers`             | Create an Organizer       |
| PUT    | `/api/admin/users/{id}/status`      | Update user status        |
| PUT    | `/api/admin/organizers/{id}/status` | Update organizer status   |
| DELETE | `/api/admin/users/{id}`             | Delete a user             |
| DELETE | `/api/admin/organizers/{id}`        | Delete an organizer       |
| GET    | `/api/admin/stats`                  | View dashboard statistics |

Protected endpoints require the appropriate authenticated session and role.

## 🗄️ Database Design

The application uses three main entities.

**User**

* Unique user ID, username, email, and hashed password.
* Full name, role, and account status.

**Event**

* Event title, description, category, date, time, venue, capacity, registration fee, and status.
* Organizer ID and organizer details.

**Registration**

* Registration ID and event reference.
* User ID and organizer ID.
* Registrant information and registration timestamp.
* Registration status.

A unique database constraint on the event and user combination helps prevent duplicate registrations.

## 🧪 Testing the Application

To verify the main workflows:

1. Log in as Admin.
2. Create a second Organizer account.
3. Log in as Organizer 1 and create an event.
4. Log in as Organizer 2 and create another event.
5. Verify each organizer manages only their own events.
6. Test unauthorized attempts to edit or delete another organizer's event.
7. Register User 1 for an event.
8. Register User 2 for another event.
9. Verify each user sees only their own registrations.
10. Log in as Admin and verify platform-wide access.

## 🔐 Security Considerations

* Passwords are stored using BCrypt hashing.
* Authentication uses HTTP sessions.
* Role and ownership checks are performed by backend controllers.
* Public registration creates only User accounts.
* Unauthorized organizer operations are rejected.
* Database credentials should be stored outside source control.

For production deployment, configure HTTPS, secure session cookies, CSRF protection appropriate to the session-based design, and stronger account provisioning. Demo credentials must not be used in a public production environment.

## 🔮 Future Enhancements

* Email notifications and event reminders.
* Online payment integration.
* Event search and advanced filtering.
* Digital tickets and QR-code check-in.
* Attendance tracking and analytics.
* Automated testing and cloud deployment.

## 🎯 Project Objective

The objective of EventEase is to provide a centralized event management platform with independent Organizer and User accounts, secure role-based access, event registration, and efficient administrative operations. The project demonstrates full-stack development, REST API design, relational database integration, session management, and authorization.

## 👩‍💻 Author

I. Asritha 

GitHub Repository: https://github.com/asritha94/EventManagementSystem

