# 🏷️ TagHub - Distributed Text Labeling Platform

TagHub is a distributed, microservices-based text annotation platform developed for machine learning dataset labeling, collaborative text annotation, and linguistic research.

The platform follows a modern **Spring Cloud microservices architecture** with **React (Vite)** on the frontend. Every component is fully containerized using **Docker** and **Docker Compose**, allowing the entire environment to be deployed with a single command.

---

# ✨ Features

- 🔐 JWT-based authentication and authorization
- 📧 Email verification & password reset using 6-digit OTP
- 👥 Multi-user collaborative labeling rooms
- 🎟️ 8-character invitation codes for joining rooms
- 🏷️ Large-scale text annotation system
- 📄 Bulk `.txt` dataset upload
- 📊 CSV export for labeled datasets
- 🔒 AES-256 encryption for sensitive text data
- ⚡ Database indexing for high-performance queries
- 🌐 Service discovery using Eureka
- 🚪 API Gateway as a single entry point
- 🐳 Fully Dockerized infrastructure

---

# 🏗️ Architecture

The system follows the **Database-per-Service** microservice architecture where every service owns its own logical PostgreSQL schema.

```
                   React (Vite)
                        │
                        ▼
              Spring Cloud Gateway
                        │
        ┌───────────────┼───────────────┐
        │               │               │
        ▼               ▼               ▼
  Auth Service     Room Service     Tag Service
        │               │               │
        └───────────────┼───────────────┘
                        │
                Eureka Discovery
                        │
                        ▼
                  PostgreSQL 15
```

---

# 📦 Services

## 🔍 Discovery Server

The Eureka Discovery Server is responsible for service registration and discovery.

- Dynamic service registration
- Service lookup
- Eliminates hardcoded IP addresses
- Enables communication between microservices

---

## 🛡️ API Gateway

The Gateway acts as the single entry point for all frontend requests.

Responsibilities:

- Dynamic request routing
- Load balancing
- CORS configuration
- Authentication filters
- Security layer

Example routing:

```
/api/v1/auth/**
        ↓
Auth Service

/api/v1/rooms/**
        ↓
Room Service

/api/v1/tags/**
        ↓
Tag Service
```

---

## 🔐 Auth Service

Responsible for user management.

Features:

- User registration
- Login
- JWT generation
- JWT validation
- Password encryption
- Email OTP verification
- Password reset

---

## 🚪 Room Service

Responsible for collaborative labeling rooms.

Features:

- Create room
- Join room
- Delete room
- Member management
- Admin permissions
- Invitation code generation
- Room lifecycle management

---

## 🏷️ Tag Service

Core labeling engine.

Features:

- Upload text datasets
- Parse `.txt` files
- Store millions of text rows
- Voting system
- Label synchronization
- CSV report generation
- Pagination support

---

## 💻 Frontend (TagUI)

Built with:

- React 18
- Vite
- Axios
- React Router
- Lucide Icons

Responsibilities:

- Authentication pages
- Dashboard
- Room management
- Annotation interface
- CSV download
- Dark modern UI

---

# 🔒 Security

## JWT Authentication

All protected endpoints require JWT authentication.

The frontend automatically injects:

- Authorization Bearer Token
- X-User-Id header

using Axios interceptors.

---

## AES-256 Encryption

Sensitive text content is encrypted using AES-256 before being stored in PostgreSQL.

Implemented with:

- Hibernate
- PostgreSQL
- `@ColumnTransformer`

This ensures that even raw database dumps remain unreadable.

---

# ⚡ Performance

To support very large datasets, several optimizations are implemented.

## Database Indexes

B-Tree indexes are created on frequently queried columns.

Examples:

- `room_id`
- `is_fully_labeled`

Benefits:

- Faster filtering
- Faster pagination
- Reduced query execution time

---

# 🛠️ Tech Stack

## Backend

- Java 17
- Spring Boot 3
- Spring Cloud
- Netflix Eureka
- Spring Cloud Gateway
- Spring Data JPA
- Hibernate
- Spring Security
- JWT
- JavaMailSender

## Frontend

- React 18
- Vite
- Axios
- React Router
- Lucide React

## Database

- PostgreSQL 15

## DevOps

- Docker
- Docker Compose
- Multi-stage Docker Builds

---

# 🚀 Getting Started

## Prerequisites

Install:

- Docker
- Docker Compose

---

## 1. Clone the Repository

```bash
git clone <repository-url>
cd TagHub
```

---

## 2. Configure Environment Variables

Create a `.env` file in the project root.

```env
JWT_SECRET_KEY=YOUR_SUPER_SECURE_SECRET_KEY

MAIL_USERNAME=your_email@gmail.com

MAIL_PASSWORD=your_google_app_password
```

---

## 3. Build and Start

```bash
docker-compose up --build -d
```

Docker will automatically:

- Build all Spring Boot services
- Build the React frontend
- Start PostgreSQL
- Initialize database schemas
- Register services with Eureka
- Launch every container

---

## 4. Wait for Initialization

On the first startup, allow approximately **1–2 minutes** for:

- PostgreSQL initialization
- Eureka registration
- Gateway route synchronization

If a temporary **503 Service Unavailable** error appears, simply wait until all services finish registering.

---

# 🌐 Application URLs

| Service | URL |
|---------|-----|
| Frontend | http://localhost:5173 |
| API Gateway | http://localhost:8080/api/v1 |
| Eureka Dashboard | http://localhost:8761 |
| PostgreSQL | localhost:5433 |

Database credentials:

```
Username: postgres
Password: mellon
```

---

# 📂 Project Structure

```
TagHub
│
├── discovery-server
├── gateway-service
├── auth-server
├── room-server
├── tag-service
├── frontend
├── docker-compose.yml
├── .env
└── README.md
```

---

# 🛑 Stop Containers

```bash
docker-compose down
```

---

# 🗑️ Remove Everything

To completely remove containers, networks, volumes, and databases:

```bash
docker-compose down -v
```

---

# 📈 Scalability

The platform is designed to be horizontally scalable.

Future improvements may include:

- Redis caching
- Kafka event streaming
- Kubernetes deployment
- ElasticSearch integration
- Prometheus monitoring
- Grafana dashboards
- CI/CD pipelines
- Automatic service scaling

---

# 📄 License

This project is intended for educational and research purposes.
