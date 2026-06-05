# Airline Management System

A robust, object-oriented console-based airline management application designed to handle fleet logistics, route planning, flight scheduling, and personnel management.

## 🚀 Key Features

- **Fleet & Logistics**: Manage aircraft models, fuel capacity, and range constraints.
- **Route Management**: Define routes and validate airplane compatibility based on distance and fuel requirements.
- **Flight Operations**: Schedule, depart, and complete flights with real-time status tracking.
- **HR Module**: Comprehensive management of employees and passengers.
- **Data Integrity**: Defensive programming with automated validation for all business operations.


## 🏗️ Architecture

The application follows a layered architecture:

- Models: Domain entities
- Repository Layer: Database access and persistence
- Service Layer: Business rules and validations
- View Layer: Console-based user interface

This separation improves maintainability, testability, and scalability.


## 🛠️ Technical Layer

- **Language**: Java (JDK 21)
- **Database**: MySQL
- **Database Access**: JDBC
- **Architecture**: Service-Repository pattern for clear separation of concerns
- **Version Control**: Git


## 🔧 Persistence Layer

The application uses plain JDBC for database interaction, implementing CRUD operations through a dedicated repository layer without relying on ORM frameworks.


## 📋 Getting Started

### Prerequisites
- JDK 21 or higher
- Database: MySQL Server
- Maven 3.x (for dependency management)
  
## 🗄️ Database Setup
1. Ensure MySQL Server is running.
2. Create the database: `CREATE DATABASE airline_db;`
3. Import the schema and initial data:
   ```bash
   mysql -u airline_admin -p airline_db < schema.sql
   ```

## ⚙️ Database Configuration

Before running the application, configure the database connection settings in the appropriate JDBC configuration class:

- Database URL
- Username
- Password
  

### Running the Application
1. Clone the repository:
   ```bash
   git clone git@github.com:vaidacutean-denisa/Airline-Management-System.git
   ```
2. Navigate to the project repository:
    ```bash
    cd Airline-Management-System
    ```
3. Compile and run using your preferred IDE (IntelliJ IDEA recommended) or Maven:
   ```bash
   mvn compile exec:java -Dexec.mainClass="airlinesystem.Main"
   ```

## 📂 Project Structure
```text
src/main/java/airlinesystem/
├── models/       # Data entities (Flight, Pilot, Airplane, etc.)
├── services/     # Business logic and validation layers
├── repository/   # Data access and persistence
└── view/         # Console UI and user interaction
```
