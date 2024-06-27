# Identity Reconciliation

This repository contains a backend service for identity reconciliation, implemented using Java Spring-Boot and PostgreSQL.

## Table of Contents
- [Introduction](#introduction)
- [Features](#features)
- [Technologies](#technologies)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation](#installation)
    - [Configuration](#configuration)
    - [Running the Application](#running-the-application)
- [Usage](#usage)
- [Pending Tasks](#pending-tasks)
    - [Docker Setup](#docker-setup)
    - [Tests](#tests)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

## Introduction

This project implements a backend service to reconcile identities from different sources. The primary goal is to merge multiple records into a single identity record based on certain matching criteria.

## Features

- Reconciliation of identities from multiple data sources
- RESTful API endpoints for identity management
- Data persistence using PostgreSQL
- Error handling and logging
- API documentation with OpenAPI

## Technologies

- Java 17
- Spring Boot 3.3.0
- PostgreSQL
- Maven

## Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:

- Java 17
- Maven
- PostgreSQL

### Installation

1. Clone the repository:
    ```sh
    git clone https://github.com/sanaullahmohammed/identity-reconciliation.git
    cd identity-reconciliation
    ```

2. Install the required dependencies:
    ```sh
    mvn clean install
    ```

### Configuration

1. Create a PostgreSQL database:
    ```sql
    CREATE DATABASE identity_reconciliation;
    ```

2. Update the `application.properties` file with your PostgreSQL credentials:
    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/identity_reconciliation
    spring.datasource.username=yourusername
    spring.datasource.password=yourpassword
    spring.jpa.hibernate.ddl-auto=update
    ```

### Running the Application

1. Start the Spring Boot application:
    ```sh
    mvn spring-boot:run
    ```

2. The application will be available at `http://localhost:8080`.

3. The swagger-ui will be available at `http://localhost:8080/swagger-ui/index.html`.

## Usage

The API provides the following endpoints:

- `POST /api/identities` - Reconcile and create a new identity

Example request to reconcile identities:
```json
{
    "email": "john.doe@example.com",
    "phone": "1234567890"
}
```

## Pending Tasks

### Docker Setup

Docker and Docker Compose are not yet set up for this project. Implementing Docker will enable easier deployment and environment management.

### Tests

The project currently lacks test coverage. Adding unit and integration tests is a pending task.

To run the tests (once implemented), use:
```sh
mvn test
```

## Contributing

Contributions are welcome! Please fork the repository and create a pull request with your changes.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a pull request

## License

Distributed under the MIT License. See `LICENSE` for more information.

## Contact

Mohammed Sanaullah - [mohammedsanaullah.dev@gmail.com](mailto:mohammedsanaullah.dev@gmail.com)

Project Link: [https://github.com/sanaullahmohammed/identity-reconciliation](https://github.com/sanaullahmohammed/identity-reconciliation)
