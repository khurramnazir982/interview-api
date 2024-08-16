# Conference Room Booking REST API

## Overview

This application is a Conference Room Booking REST API designed for internal use within a company. It allows users to book one of four available conference rooms based on their capacity and availability. The system ensures optimal booking by selecting the most suitable room for the number of attendees while considering maintenance schedules and other constraints.

### Conference Rooms and Their Capacities:

- **Amaze**: 3 Persons
- **Beauty**: 7 Persons
- **Inspire**: 12 Persons
- **Strive**: 20 Persons

### Maintenance Schedule:

Each conference room undergoes maintenance at the following times every day:
- 09:00 - 09:15
- 13:00 - 13:15
- 17:00 - 17:15

### Key Features and Rules:

1. **Same-Day Booking**: Users can only book rooms for the current date.
2. **15-Minute Intervals**: Bookings must be made in 15-minute increments (e.g., 14:00 - 14:15, 14:00 - 14:30).
3. **First Come, First Serve**: Bookings are processed in the order they are received.
4. **Optimal Room Allocation**: The system allocates the most suitable room based on the number of attendees. If the ideal room is unavailable, it moves to the next suitable option.
5. **Maintenance Constraint**: Bookings cannot overlap with maintenance times. The system will notify the user if their desired time conflicts with the maintenance schedule.
6. **Capacity Limits**: The number of attendees must be greater than 1 and within the room's capacity.

## Technology Stack

- **Java 11**
- **Spring Boot**
- **Maven**
- **H2 In-Memory Database**

## Getting Started

### Prerequisites

Ensure that the following tools are installed on your system:
- **Java 11** or above
- **Maven**

### Setup Instructions

1. **Clone the Repository**:
   ```bash
   git clone <repository-url>
   cd <repository-directory> `

2. **Build the Project**:

    Navigate to the project directory and run:

    `mvn clean install`

3. **Run the Application**: You can start the application by running:

    `mvn spring-boot:run`

    The application will start and be accessible at `http://localhost:8080`.

### Testing via Postman

#### 1\. **Booking a Room**

**Endpoint**: `POST /api/bookings`

**Request Body**:


`{
"startTime": "14:00",
"endTime": "15:00",
"numberOfPeople": 5
}`

**Description**: This request attempts to book a room for 5 people from 14:00 to 15:00. The system will allocate the most suitable room based on the number of people and availability.

**Response Example**:


`{
"message": "Room 'Beauty' booked successfully for 5 people from 14:00 to 15:00."
}`

#### 2\. **Checking Available Rooms**

**Endpoint**: `GET /api/rooms/availability?startTime=14:00&endTime=15:00`

**Request Parameters**:

-   `startTime`: The start time of the booking window (e.g., `14:00`).
-   `endTime`: The end time of the booking window (e.g., `15:00`).

**Description**: This request checks for the availability of rooms between 14:00 and 15:00.

**Response Example**:


`{
"availableRooms": [
{
"name": "Strive",
"capacity": 20
},
{
"name": "Inspire",
"capacity": 12
}
]
}`

#### 3\. **Error Handling**

**Example Error Response**: If a booking request is made for a time overlapping with maintenance, the response might look like this:

`{
"error": "The requested time overlaps with maintenance windows."
}`

Database
--------

This application uses an in-memory H2 database, which is automatically configured and does not require manual setup. The database schema is generated on application startup.

### Accessing H2 Console

You can access the H2 database console at:


`http://localhost:8080/h2-console`

Use the following credentials:

-   **JDBC URL**: `jdbc:h2:mem:testdb`
-   **Username**: `sa`
-   **Password**: (leave blank)

Extensibility
-------------

-   **Room Management**: The system can be extended to allow dynamic addition or removal of rooms.
-   **Multi-Day Booking**: Currently, bookings are restricted to the same day, but this can be extended to support multi-day or future bookings.
-   **User Authentication**: User roles can be added to manage booking rights and access control.

Testing
-------

The application includes unit tests for core functionalities. To run the tests:

`mvn test`

Conclusion
----------

This Conference Room Booking REST API is a robust, efficient system designed to manage room bookings in a corporate environment. It follows good design principles, ensuring maintainability, testability, and extensibility. The RESTful nature of the API allows for easy integration with front-end applications or other systems within the organization.