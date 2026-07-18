# MediTracker

A microservices-based patient health monitoring system built with Spring Boot and React.

MediTracker allows patients to submit vital readings online which are automatically checked against clinical thresholds. If a reading is abnormal, the system generates a real-time alert to the patient's assigned doctor via Apache Kafka. The system also supports appointment booking and prescription management.

---

## Services

| Service | Port | Database | Description |
|---|---|---|---|
| patient-service | 8081 | patient_db | Manages doctors and patients |
| appointment-service | 8082 | appointment_db | Handles appointment booking and cancellation |
| medication-service | 8083 | medication_db | Manages prescriptions |
| vitals-service | 8084 | vitals_db | Stores vital readings and triggers alerts |
| notification-service | 8085 | notification_db | Consumes Kafka events and generates doctor alerts |
| client | 5173 (dev) | — | React frontend |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language / Build | Java 21, Maven, Node.js |
| Framework | Spring Boot 3.4.x |
| Messaging | Apache Kafka |
| Database | MySQL, Spring Data JPA / Hibernate |
| Containerisation | Docker, Docker Compose |
| Orchestration | Kubernetes (Minikube) |
| Testing | JUnit 5, Mockito, Spring Boot Test |
| Client | React (Vite), React Router, Axios, Bootstrap |

---

## Vital Alert Thresholds

| Vital Type | Normal Range | Alert Condition |
|---|---|---|
| Blood Pressure | Systolic 90–139 mmHg, Diastolic 60–89 mmHg | Outside either range |
| Heart Rate | 60–100 BPM | Below 60 or above 100 |
| Blood Glucose | 4.0–7.8 mmol/L | Below 4.0 or above 7.8 |
| Temperature | 36.1–37.8°C | Below 36.1 or above 37.8 |
| SpO2 | 95–100% | Below 95% |

---

## Prerequisites

- Java 21
- Maven
- Node.js and npm
- Docker Desktop
- IntelliJ IDEA (recommended)

---

## Running the Project

### 1. Start the databases

```bash
docker compose up -d
```

### 2. Start each Spring Boot service

Open each service folder in IntelliJ and run the main application class, or from the terminal:

```bash
cd patient-service
mvn spring-boot:run
```

Repeat for each service on its respective port.

### 3. Start the React client

```bash
cd client
npm run dev
```

Open `http://localhost:5173` in your browser.

---

## Project Structure

```text
meditracker/
├── patient-service/         # Doctor and patient management (port 8081)
├── appointment-service/     # Appointment booking (port 8082)
├── medication-service/      # Prescription management (port 8083)
├── vitals-service/          # Vital readings and Kafka producer (port 8084)
├── notification-service/    # Kafka consumer and alerts (port 8085)
├── client/                  # React frontend (port 5173)
├── docker-compose.yml       # Database containers
└── README.md
```

---

## API Endpoints

### Patient Service (port 8081)

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/doctors | Register a new doctor |
| GET | /api/doctors/{id} | Get doctor by ID |
| GET | /api/doctors | Get all doctors |
| POST | /api/patients | Register a new patient |
| GET | /api/patients/{id} | Get patient by ID |
| GET | /api/patients?doctorId={id} | Get all patients for a doctor |

### Appointment Service (port 8082)

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/appointments | Book an appointment |
| GET | /api/appointments?patientId={id} | Get appointments for a patient |
| GET | /api/appointments?doctorId={id} | Get appointments for a doctor |
| PATCH | /api/appointments/{id}/cancel | Cancel an appointment |

### Medication Service (port 8083)

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/prescriptions | Issue a prescription |
| GET | /api/prescriptions?patientId={id} | Get prescriptions for a patient |

### Vitals Service (port 8084)

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/vitals | Submit a vital reading |
| GET | /api/vitals?patientId={id} | Get vital reading history for a patient |

### Notification Service (port 8085)

| Method | Endpoint | Description |
|---|---|---|
| GET | /api/notifications?doctorId={id} | Get all notifications for a doctor |
| GET | /api/notifications/unread?doctorId={id} | Get unread notifications for a doctor |
| PATCH | /api/notifications/{id}/read | Mark a notification as read |

---

## Kafka Flow

1. Patient submits a vital reading via the React client
2. Vitals Service validates and saves the reading
3. Vitals Service checks the reading against clinical thresholds
4. If abnormal, Vitals Service publishes a `VitalAlertEvent` to the `vitals-alert` Kafka topic
5. Notification Service consumes the event and creates a notification record for the assigned doctor
6. Doctor sees the alert on their notifications page in real time

---

## Testing

Each service includes unit tests (JUnit 5 + Mockito) and integration tests (Spring Boot Test + MockMvc). Run all tests for a service with:

```bash
cd patient-service
mvn test
```
