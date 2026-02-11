# Requirements Specification: Unified User Management System (UUMS)

## 1. Project Overview
A centralized authentication and authorization hub designed for multi-tenant application support. It provides Single Sign-On (SSO), robust traffic tracking, and administrative controls with high scalability for AWS deployment.

---

## 2. Technical Architecture & Stack

### Core Frameworks
* **Backend:** Spring Boot (Java), Spring Security (OAuth2/OIDC), Spring Batch (for bulk processing/reporting).
* **Frontend:** React.js with Tailwind CSS or Material UI.
* **Build Tool:** Maven (Multi-module structure).

### Database & Migration
* **Production/UAT:** PostgreSQL.
* **Local/Testing:** H2 (In-memory).
* **Version Control:** Liquibase using `.sql` format for DDL (schema) and DML (data).

### Infrastructure & DevOps
* **Environment Management:** Profile-based `application.yml` (local, dev, uat, prod).
* **Containerization:** `Dockerfile` for each module and `docker-compose.yml` for local orchestration.
* **Automation:** Shell scripts for `start.sh`, `restart.sh`, and `stop.sh`.
* **Cloud:** AWS-ready with configurations for Auto-Scaling Groups (ASG) and RDS.

### Project Structure
```text
/uums-parent
  ├── /uums-api (Spring Boot Backend)
  ├── /uums-batch (Spring Batch Jobs)
  ├── /uums-ui (React Frontend)
  ├── /memory (Chronological logs in .md format)
  ├── /scripts (Start/Stop/Restart)
  └── docker-compose.yml
```

## 3. Functional Requirements

### 3.1 Authentication & Identity Management
* **Single Sign-On (SSO):** Centralized JWT-based authentication compatible with multiple client applications.
* **Registration & Login:** Dual-access via React UI and secure REST endpoints.
* **Multi-Factor Authentication (MFA):** Mandatory two-way authentication using OTP (Email/SMS) or Security Questions.
* **Social Login:** Out-of-the-box integration for **Google, Meta, and Microsoft** using OAuth2, with an extensible provider interface.
* **Account Recovery:** Password reset flow utilizing security questions and verified OTP.

### 3.2 Tenant & Service Management
* **Service Onboarding:** Self-service registration for new applications to generate `API_KEY` credentials.
* **Pricing & Tiers:** Infrastructure for configurable pricing tiers and limit tracking per service.
* **Request Throttling:** Built-in rate limiting to safeguard against brute-force attacks and service abuse.

### 3.3 User Roles & Permissions
* **Granular RBAC:** Ability to create custom roles and map them to specific actions/permissions.
* **Admin Dashboard:**
    * Full User CRUD (Create, Read, Update, Delete).
    * Account state management (Lock, Unlock, Disable, Enable).
    * Manual password overrides and role assignments.
* **Account Locking:** Automated locking mechanism after a configurable number of failed login attempts.

### 3.4 Social & Interactive Features
* **Account Linking:** Capability to link multiple accounts with defined relationships (e.g., Family, Corporate).
* **Follow System:** Social graph allowing users to follow/unfollow and view shared activity feeds.
* **In-App Messaging:** Secure communication channel for users within the platform.
* **Auto-Moderation:** Automated scanning of user-generated content to identify and flag unsafe words or profanity.
* **Profile Personalization:** Support for animated profile pictures (Library of 20 suggested animated assets).

### 3.5 Traffic Tracking & Reporting
* **Usage Analytics:** Track site traffic at both the Application and individual User levels via REST interceptors.
* **Admin Reporting:** Visualized reports for traffic trends, aggregated usage data, and per-app performance.
* **Smart Alerts:** Automated notifications (Email/SMS) triggered by:
    * Traffic spikes/anomalies.
    * Pricing tier limit thresholds.
    * Custom security events.

---

## 4. Technical Specifications & Validation

### 4.1 Data Integrity & Security
* **Safeguard Rules:** Strict regex-based validation for usernames and high-entropy password requirements.
* **Full-Stack Validation:**
    * **Frontend:** Real-time validation for Email, Username, Phone, Address, and DOB.
    * **Backend:** JSR-303 Bean Validation for all incoming DTOs.
* **Database Migration:** Liquibase managed scripts for DDL (Table structures) and DML (Initial data/Roles).

### 4.2 Testing Strategy
* **Unit Testing:** High coverage using JUnit 5 and Mockito.
* **Integration Testing:** Spring Boot Test with H2 in-memory profile.
* **Behavioral Testing:** BDD (Behavior Driven Development) using Cucumber to validate functional user stories.

### 4.3 Deployment & Scaling
* **Environment Profiles:** Isolated configs for `local`, `dev`, `uat`, and `prod`.
* **Containerization:** Multi-stage Dockerfiles for optimized image sizes.
* **AWS Compatibility:** Designed for **AWS ECS/EKS** with on-demand scaling and **RDS** for managed PostgreSQL.

---

## 5. Documentation & Onboarding

### 5.1 Developer Resources
* **API Documentation:** Interactive Swagger/OpenAPI documentation.
* **Boilerplate Code:** Provided snippets for integrating the UUMS via REST for:
    * Java (Spring RestTemplate/WebClient)
    * Python (Requests)
    * React (Custom Hooks)
    * Node.js

### 5.2 Initial Data (DML)
* **Seed Users:**
    * **Admin:** `admin_uums` / `SecureAdmin@2026`
    * **Test User:** `test_user_01` / `Password@123`

---

## 6. Memory & Logs
All project architectural decisions and chronological progress are stored in the `/memory` folder as `.md` files to maintain a searchable development history.
