# MediTracker

A microservices-based patient health monitoring system built with Spring Boot and React. 
Patients submit vital readings which are automatically checked against clinical thresholds. 
If a reading is abnormal, the system generates a real-time alert to the patient's assigned 
doctor. The system also supports appointment booking and prescription management.

---

## Architecture

MediTracker is built using a microservices architecture. Each service is independently 
deployed, has its own MySQL database, and communicates with other services via REST APIs.

```text
Client (React)
│
├── REST → patient-service (port 8081)
│   └── patient_db (MySQL)
│
├── REST → appointment-service (port 8082)
│   └── appointment_db (MySQL)
│
├── REST → vitals-service (port 8084)
│   ├── vitals_db (MySQL)
│   └── REST → notification-service (on abnormal reading)
│
└── REST → notification-service (port 8085)
    └── notification_db (MySQL)
```

Prescriptions are managed within the patient-service alongside doctor and patient records.

---

## Services

| Service | Port | Database | Description |
|---|---|---|---|
| patient-service | 8081 | patient_db | Manages doctors, patients and prescriptions |
| appointment-service | 8082 | appointment_db | Handles appointment booking and cancellation |
| vitals-service | 8084 | vitals_db | Stores vital readings and triggers alerts |
| notification-service | 8085 | notification_db | Receives alerts and notifies doctors |
| client | 5173 (dev) / 3000 (Docker) | — | React frontend |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language / Build | Java 21, Maven, Node.js |
| Framework | Spring Boot 4.1.0 |
| Database | MySQL 8, Spring Data JPA / Hibernate |
| Containerisation | Docker, Docker Compose |
| Testing | JUnit 5, Mockito, Spring Boot Test, H2 (test) |
| CI/CD | GitHub Actions |
| Client | React (Vite), React Router, Axios, Bootstrap |

---

## Vital Alert Thresholds

When a patient submits a reading outside these ranges, an alert is automatically 
sent to their assigned doctor.

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

---

## Running the Project

### 1. Clone the repository

```bash
git clone https://github.com/yourusername/meditracker.git
cd meditracker
```

### 2. Build all service JARs

```bash
cd patient-service && mvn clean package -DskipTests && cd ..
cd appointment-service && mvn clean package -DskipTests && cd ..
cd vitals-service && mvn clean package -DskipTests && cd ..
cd notification-service && mvn clean package -DskipTests && cd ..
```

### 3. Start all services with Docker Compose

```bash
docker compose up --build
```

This starts all four MySQL databases, all four Spring Boot services, 
and the React client. Allow 10–15 minutes on first run for MySQL to initialise.

### 4. Access the application

Open `http://localhost:3000` in your browser.

To register a doctor and patients for testing, use the Postman requests 
in the section below.

---

## Running Tests

Run tests for each service individually:

```bash
cd patient-service && mvn test
cd appointment-service && mvn test
cd vitals-service && mvn test
cd notification-service && mvn test
```

Tests use an in-memory H2 database and do not require Docker or MySQL to be running.

| Service | Unit Tests | Integration Tests | Total |
|---|---|---|---|
| patient-service | 33 | 35 | 68 |
| vitals-service | 40 | 10 | 50 |
| appointment-service | 19 | 19 | 38 |
| notification-service | 12 | 12 | 24 |
| **Total** | **104** | **76** | **180** |

Tests also run automatically on every push to main via GitHub Actions.

---

## API Endpoints

### Patient Service (port 8081)

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/doctors | Register a new doctor |
| GET | /api/doctors/{id} | Get doctor by ID |
| GET | /api/doctors | Get all doctors |
| POST | /api/doctors/login | Doctor login |
| POST | /api/patients | Register a new patient |
| GET | /api/patients/{id} | Get patient by ID |
| GET | /api/patients | Get all patients (optional ?doctorId filter) |
| POST | /api/patients/login | Patient login |
| POST | /api/prescriptions | Issue a prescription |
| GET | /api/prescriptions | Get prescriptions (?patientId or ?doctorId, optional ?status=ACTIVE) |

### Appointment Service (port 8082)

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/appointments | Book an appointment |
| GET | /api/appointments | Get appointments (?patientId or ?doctorId, optional ?upcoming=true) |
| GET | /api/appointments/pending | Get pending appointments for a doctor |
| PATCH | /api/appointments/{id}/confirm | Confirm a pending appointment |
| PATCH | /api/appointments/{id}/cancel | Cancel an appointment |

### Vitals Service (port 8084)

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/vitals | Submit a vital reading |
| GET | /api/vitals | Get readings (?patientId, optional ?vitalType filter) |

### Notification Service (port 8085)

| Method | Endpoint | Description |
|---|---|---|
| GET | /api/notifications | Get all notifications for a doctor (?doctorId) |
| GET | /api/notifications/unread | Get unread notifications (?doctorId) |
| PATCH | /api/notifications/{id}/read | Mark a notification as read |
| POST | /api/notifications/alert | Receive alert from vitals-service (internal) |

---

## Demo Postman Requests

Use these in order to seed the database after a fresh start.

**Register a doctor:**
```json
POST http://localhost:8081/api/doctors
{
  "name": "Dr. Sarah Murphy",
  "email": "sarah.murphy@meditracker.com",
  "specialisation": "Cardiology",
  "password": "password123"
}
```

**Register a patient:**
```json
POST http://localhost:8081/api/patients
{
  "name": "Séan O'Brien",
  "dateOfBirth": "1972-05-14",
  "email": "sean.obrien@email.ie",
  "phone": "0851234567",
  "address": "14 Grafton Street, Dublin 2",
  "medicalHistory": "Hypertension, Type 2 Diabetes",
  "allergies": "Penicillin",
  "assignedDoctorId": 1,
  "password": "password123"
}
```

**Submit an abnormal vital reading (triggers alert):**
```json
POST http://localhost:8084/api/vitals
{
  "patientId": 1,
  "vitalType": "BLOOD_PRESSURE",
  "systolic": 155,
  "diastolic": 95
}
```

**Check doctor notifications:**
```http
GET http://localhost:8085/api/notifications?doctorId=1
```

---

## Project Structure

```text
meditracker/
├── patient-service/          # Doctor, patient and prescription management (port 8081)
├── appointment-service/      # Appointment booking and confirmation (port 8082)
├── vitals-service/           # Vital readings and alert triggering (port 8084)
├── notification-service/     # Alert storage and doctor notifications (port 8085)
├── client/                   # React frontend (port 5173 dev / 3000 Docker)
├── docker-compose.yml        # All services and databases
├── .github/
│   └── workflows/            # GitHub Actions CI pipeline
└── README.md
```

---

## CI/CD

GitHub Actions automatically runs all tests on every push to main. 
Each service is tested in parallel on a separate Ubuntu runner. 
Results are visible in the Actions tab of the repository.

---

## Author

Eoin - Final Year Project 2026