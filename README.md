# 🏷️ TagHub - Distributed Text Labeling Platform

TagHub is a highly scalable, microservices-based **distributed text annotation and data processing platform** designed for training machine learning models and linguistic research.

Built with **Spring Boot (Spring Cloud)** on the backend and **React (Vite)** on the frontend, the entire infrastructure is fully containerized and orchestrated using **Docker and Docker Compose** for seamless deployment and resilience.

---

## 🏗️ System Architecture & Service Components

The platform utilizes a modern, loosely-coupled microservices architecture where each service owns its distinct logical database schema (Database-per-service paradigm):

### 1. 🔍 Eureka Discovery Server (`discovery-server`)
* **Service Registry:** Acts as the centralized registry where all active microservices (Auth, Room, Tag, Gateway) dynamically register themselves upon startup.
* Eliminates hardcoded IP addresses and ports, allowing services to locate and communicate with each other dynamically (Service Discovery).

### 2. 🛡️ Spring Cloud Gateway (`gateway-service`)
* **Single Entry Point:** The unified gateway routing all incoming HTTP requests from the React frontend.
* Dynamically proxies requests to appropriate destination microservices based on URL path matchers (e.g., `/api/v1/auth/**` -> Auth Service) by querying Eureka.
* Centrally handles Cross-Origin Resource Sharing (CORS) configurations and network edge security filters.

### 3. ⚙️ Core Microservices
Each independent service manages its own workflow data within a shared PostgreSQL database instance utilizing schema isolation:
* **🔐 Auth Service (`auth-server`):** Manages user registration, secure authentication, stateless JWT token generation/validation, and secure password reset workflows using a **6-digit OTP (One-Time Password)** system via email.
* **🚪 Room Service (`room-server`):** Manages multi-user collaborative labeling environments, generates unique 8-character room invitation tokens, orchestrates Admin/Member authorization matrix, and tracks room lifecycles.
* **🏷️ Tag Service (`tag-service`):** The core engine executing bulk `.txt` text dataset parsing, streaming database inserts, concurrent voting/annotation synchronization logic, and dynamic CSV report building.

### 4. 💻 TagUI (Frontend)
* A high-performance Single Page Application (SPA) built using React 18 and Vite.
* Consumes backend RESTful APIs via the centralized API Gateway and offers a sleek, modern, production-grade dark-themed user interface.

---

## 🔒 Advanced Security & Performance Enhancements

* **Encryption at Rest:** To meet strict corporate data compliance rules, sensitive text fields (`content` column inside `tag-service`) are seamlessly encrypted at the disk layer using the **AES-256** cryptographic algorithm via the joint integration of PostgreSQL and Hibernate (`@ColumnTransformer`). Data remains 100% secure even during complete database dumps or data leaks.
* **High-Performance Database Indexing (B-Tree):** To guarantee lightning-fast query response times and effortless pagination across datasets containing millions of rows, custom database indexes are pre-configured over high-traffic columns (`room_id` and `is_fully_labeled`).
* **Automated Request Interception:** The Axios API wrapper layer on the frontend transparently injects user session JWT tokens and `X-User-Id` HTTP headers into every outgoing request, eliminating overhead and bolstering API route security.

---

## 🚀 Tech Stack

* **Backend:** Java 17, Spring Boot 3, Spring Cloud (Netflix Eureka, API Gateway), Spring Data JPA, Hibernate ORM, JWT Security, JavaMailSender
* **Frontend:** React 18, Vite, Axios HTTP Client, Lucide React (Icons), React Router
* **Database:** PostgreSQL 15
* **DevOps & Infrastructure:** Docker, Docker Compose, Multi-stage Docker Builds (Optimized, lightweight container layers)

---

## 🛠️ Infrastructure Setup & Quick Start

Since the entire application ecosystem is fully dockerized, spinning up the environment takes only a few minutes.

### 📋 Prerequisites
* Ensure **Docker** and **Docker Compose** are installed and running on your host system.

### 1. Configure Environment Variables (`.env`)
Create a file named `.env` in the root project directory (alongside the `docker-compose.yml` file) and provide your configuration parameters based on the template below:

```env
JWT_SECRET_KEY=YOUR_SUPER_SECURE_JWT_SECRET_KEY_PASSPHRASE_XYZ123
MAIL_USERNAME=your_gmail_address@gmail.com
MAIL_PASSWORD=your_16_character_google_app_specific_password
