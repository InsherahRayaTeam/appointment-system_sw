# Appointment Scheduling System

## Group Members
Insherah Dwikat 
Raya Barakat   
## Project Overview
This project is an Appointment Scheduling System developed for the Software Engineering course.
It is built incrementally using Java, Maven, JUnit 5, JaCoCo, and Mockito, following a layered architecture and applying software engineering principles.

## Main Features
- Administrator login and logout
- View available appointment slots
- Book appointments
- Modify and cancel appointments
- Reservation management for administrators
- Appointment reminders and notifications
- Multiple appointment types
- Type-specific booking rules
- Waitlist support
- Calendar export / calendar integration

## Project Structure
- `domain` → core entities and enums
- `service` → business logic
- `repository` → data access and in-memory repositories
- `notification` → notification abstractions and implementations
- `presentation.gui` → GUI screens and controllers

## Technologies Used
- Java
- Maven
- JUnit 5
- JaCoCo
- Mockito
- Swing GUI

## Testing
The project includes automated unit tests for the core logic.
Mockito is used for mocking dependencies in tests.
JaCoCo is used to measure test coverage.

## Documentation
- JavaDoc is included
- UML class diagram is included

## How to Run
1. Open the project in IntelliJ IDEA or Eclipse
2. Ensure Maven dependencies are loaded
3. Run the tests using:
   `mvn test`
4. Run the application from the `Main` class

## Notes
- This project follows a layered architecture
- The project includes GUI support for user and administrator workflows
