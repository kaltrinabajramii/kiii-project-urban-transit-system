# Urban Public Transit System

A modern full-stack web application demonstrating best practices with a Spring Boot (Java 17) backend, React (TypeScript) frontend, PostgreSQL database, containerization, Kubernetes orchestration, and CI/CD automation using GitHub Actions and AWS EC2.

---

## Technologies Used

### Backend
- **Spring Boot (Java 17):** REST API development framework with auto configuration and extensive ecosystem.
- **Spring Data JPA & Hibernate:** Object-relational mapping to PostgreSQL with entity-based schema management.
- **PostgreSQL:** Reliable, ACID-compliant relational database.
- **Spring Security:** Authentication and authorization management.
- **Lombok:** Boilerplate code reduction via annotations.
- **Spring Boot DevTools:** Development productivity enhancements.

### Frontend
- **React:** Component-based UI library.
- **TypeScript:** Typed superset of JavaScript enabling safer code.
- **Axios:** HTTP client to interface with backend REST APIs.
- **React Router:** Single page app routing solution.
- **React Context API:** State and auth management.

### DevOps & Infrastructure
- **Docker:** Containerizes backend, frontend, and database services.
- **Docker Compose:** Multi-container local orchestration with volume persistence.
- **k3d (Kubernetes):** Local Kubernetes environment for production-like testing.
- **GitHub Actions:** CI/CD pipeline automating building, testing, and AWS deployment.
- **AWS EC2:** Cloud compute environment hosting production containers.

---

## Project Structure
kiii-project-urban-transit-system/
├── backend/ # Spring Boot application
├── frontend/ # React application (TypeScript)
├── .gitignore
├── .env.example
└── README.md


---

## License

This project is provided for educational and demonstration purposes only.
