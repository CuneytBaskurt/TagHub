# TagHub - Distributed Text Labeling Platform

TagHub is a robust, microservices-based web application designed for collaborative text annotation and labeling. Built with **Spring Boot** on the backend and **React (Vite)** on the frontend, the entire platform is orchestrated using **Docker** and **Docker Compose**, making it highly scalable, resilient, and easy to deploy.

### 1. Eureka Discovery Server (`discovery-server`)
Acts as the registry for all backend microservices. Every service (Auth, Room, Tag, Gateway) registers itself here upon startup. This eliminates the need for hardcoded IP addresses and ports, allowing services to find and communicate with each other dynamically.

### 2. Spring Cloud Gateway (`gateway-service`)
The single entry point for all client requests coming from the React frontend. It dynamically routes incoming HTTP requests to the appropriate microservice based on the URL path (e.g., `/api/v1/auth/**` goes to Auth Service) by querying the Eureka Discovery Server. It also handles Cross-Origin Resource Sharing (CORS) centrally.

### 3. Core Microservices
Each service is fully independent, owning its own logical database schema within a shared PostgreSQL instance.
* **Auth Service (`auth-server`)**: Handles user registration, login, JWT token generation/validation, and password reset operations (via email OTP).
* **Room Service (`room-server`)**: Manages collaborative labeling rooms, invite codes, participant roles (Admin/Member), and room lifecycles.
* **Tag Service (`tag-server`)**: Core engine for text dataset uploads, real-time labeling logic, vote tallying, and CSV report generation. Features built-in data encryption for sensitive text chunks.

### 4. TagUI (Frontend)
A sleek, modern Single Page Application (SPA) built with React and Vite. It consumes the REST APIs exposed by the API Gateway and provides an interactive workspace for real-time text labeling.

---

## 🚀 Technologies Used
* **Backend:** Java 17, Spring Boot 3, Spring Cloud (Netflix Eureka, API Gateway), Spring Data JPA, Hibernate, JWT, JavaMailSender
* **Frontend:** React 18, Vite, Axios, Lucide React (Icons), React Router
* **Database:** PostgreSQL 15
* **DevOps:** Docker, Docker Compose, Multi-stage builds

---

## 🛠 Prerequisites

Before running the project, ensure you have the following installed on your system:
* [Docker](https://docs.docker.com/get-docker/)
* [Docker Compose](https://docs.docker.com/compose/install/)

---

## ⚙️ Quick Start (Docker Installation)

Since the entire platform is Dockerized, getting it up and running takes only a few commands.

### 1. Environment Variables
To keep sensitive data secure, the project requires a `.env` file in the root directory (where `docker-compose.yml` is located). Create a `.env` file and add your credentials:

```env
JWT_SECRET_KEY=YOUR_SUPER_SECRET_JWT_KEY_HERE
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
```
*(Note: If using Gmail, you must generate an "App Password" from your Google Account settings, not your standard email password.)*

### 2. Start the Cluster
Open your terminal in the root directory and run:
```bash
docker-compose up --build -d
```
Docker will pull the necessary images, compile the Java and React code using multi-stage builds, initialize the PostgreSQL databases automatically via `init-dbs.sql`, and start all 6 containers.

> **Note:** On the very first run, it may take 1-2 minutes for the Eureka Discovery Server to fully register all microservices and propagate the routes to the API Gateway. If you receive a `503 Service Unavailable` error initially, simply wait a moment and refresh.

### 3. Access the Applications
Once all containers are running and healthy, you can access the platform at:
* **TagHub UI (Frontend):** [http://localhost:5173](http://localhost:5173)
* **API Gateway:** `http://localhost:8080/api/v1/...`
* **Eureka Dashboard:** [http://localhost:8761](http://localhost:8761)
* **PostgreSQL Database:** `localhost:5433` (Username: `postgres`, Password: `mellon`)

---

## 🛑 Stopping the Application
To stop all running microservices gracefully without losing your database data:
```bash
docker-compose down
```
To stop the services AND wipe the database completely (useful for a fresh start):
```bash
docker-compose down -v
```
