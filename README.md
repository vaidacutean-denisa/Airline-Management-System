# Airline Management System

A robust, object-oriented console-based application designed to manage airline operations, including fleet logistics, route networks, flight scheduling, and personnel management.

## 🚀 Key Features

- **Fleet & Logistics**: Manage aircraft models, fuel capacity, and range constraints.
- **Route Management**: Define routes and validate airplane compatibility based on distance and fuel requirements.
- **Flight Operations**: Schedule, depart, and complete flights with real-time status tracking.
- **HR Module**: Comprehensive management of employees and passengers.
- **Data Integrity**: Defensive programming with automated validation for all business operations.

## 🛠️ Technical Layer

- **Language**: Java (JDK 21)
- **Architecture**: Service-Repository pattern for clear separation of concerns.
- **Version Control**: Git

## 📋 Getting Started

### Prerequisites
- JDK 21 or higher
- Maven (for dependency management)

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

## Project Structure
```text
src/main/java/airlinesystem/
├── models/       # Data entities (Flight, Pilot, Airplane, etc.)
├── services/     # Business logic and validation layers
├── repository/   # Data access and persistence
└── view/         # Console UI and user interaction
```
