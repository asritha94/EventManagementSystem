# EventEase – Role-Based Event Management System

A full-stack Event Management System built with **Java 17, Spring Boot 3.4.3, Spring Data JPA, Hibernate, MySQL, HTML, CSS and JavaScript**. The existing EventEase UI is preserved and the application now implements strict role-based access control and account-level data isolation.

## Roles

### ADMIN – exactly one
- Username: `admin`
- Password: `admin123`
- System-wide access to users, organizers, events and registrations.
- Can create/manage organizer accounts.
- Can manage any event and registration.
- Normal registration can never create an ADMIN account.

### ORGANIZER – multiple
Demo account:
- `organizer1 / organizer123`

The demo environment starts with one Organizer account. Admin can create additional Organizer accounts at any time. Each organizer has a unique user ID and can create, edit, delete and manage registrations only for events whose `organizerId` equals the logged-in organizer's ID.

### USER – multiple
Demo account:
- `user1 / user123`

The demo environment starts with one User account. Users can also create additional USER accounts from the Login dialog. Public registration always creates `USER`; it cannot create ADMIN or ORGANIZER.

## Data isolation

Ownership is based on stable IDs, not display names or browser-supplied ownership fields.

- `Event.organizerId` identifies the organizer who owns an event.
- `Registration.userId` identifies the user who registered.
- `Registration.organizerId` identifies the event owner.
- User profile data is loaded from the authenticated session.
- `/api/registrations/my` returns only the logged-in user's registrations.
- Organizer registration views are restricted to their own events.
- Admin can view all registrations.
- Backend authorization returns HTTP 403 for unauthorized organizer/resource access.

The previous email-only registration lookup is retained only as a protected compatibility endpoint: a USER can query only their own email, while ADMIN can use it for administration.

## Main APIs

### Authentication
- `POST /api/auth/login`
- `POST /api/auth/register` – USER only
- `GET /api/auth/me`
- `PUT /api/auth/profile`
- `POST /api/auth/logout`

### Events
- `GET /api/events`
- `GET /api/events/{id}`
- `POST /api/events` – ADMIN/ORGANIZER
- `PUT /api/events/{id}` – ADMIN or owning ORGANIZER
- `DELETE /api/events/{id}` – ADMIN or owning ORGANIZER
- `GET /api/events/manage` – ADMIN all / ORGANIZER own
- `GET /api/events/{id}/registration-count`

### Registrations
- `POST /api/registrations` – authenticated USER
- `GET /api/registrations/my` – current USER only
- `GET /api/registrations` – ADMIN only
- `GET /api/registrations/event/{eventId}` – ADMIN or owning ORGANIZER
- `DELETE /api/registrations/{id}` – ADMIN, owning USER, or event-owning ORGANIZER

### Admin
- `GET /api/admin/users`
- `GET /api/admin/organizers`
- `POST /api/admin/organizers`
- `PUT /api/admin/users/{id}/status`
- `PUT /api/admin/organizers/{id}/status`
- `DELETE /api/admin/users/{id}`
- `DELETE /api/admin/organizers/{id}`
- `GET /api/admin/stats`

## Run

1. Install JDK 17+, Maven and MySQL 8+.
2. Set the MySQL password in `src/main/resources/application.properties`.
3. Import the project as an existing Maven project.
4. Run `EventManagementApplication.java`.
5. Open `http://localhost:8080`.

The application uses `spring.jpa.hibernate.ddl-auto=update`. On startup the initializer creates the demo accounts and migrates legacy event/registration ownership when possible.

## Required role-isolation test

The project starts with exactly three demo accounts: one ADMIN, one ORGANIZER and one USER.

1. Login as `organizer1 / organizer123`.
2. Create `Java Workshop`.
3. Logout and login as `admin / admin123`.
4. From **Manage Organizers**, create a second organizer account (for example `organizer2`).
5. Logout and login using the newly created Organizer 2 account.
6. Create `React Workshop`.
7. Organizer 2 sees only React Workshop in **My Events**.
8. Attempt to update/delete Java Workshop as Organizer 2: backend must return **403 Forbidden**.
9. Login as Organizer 1: Java Workshop can be updated/deleted; React Workshop cannot.
10. Login as Admin: both events are visible and manageable.
11. Login as `user1 / user123` and register for Java Workshop.
12. Create another USER account from the public registration form (for example `user2`).
13. Logout and login as the newly created User 2: User 1's registration is not shown.
14. User 2 registers for React Workshop.
15. Login again as User 1: only User 1's registration is shown.
16. Verify direct API calls cannot bypass the same restrictions.

## Important

This project uses HTTP sessions for authentication and BCrypt for password storage. The frontend hides role-inappropriate controls for usability, but authorization is independently enforced by the backend.