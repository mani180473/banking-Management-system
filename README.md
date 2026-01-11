🏦 Banking Management System (Java Web Application)

A simple Banking Management System built using Java Servlets, Maven, Apache Tomcat, and MySQL, implementing core banking operations such as user registration, login, deposit, withdrawal, and balance enquiry.

This project follows a clean Maven structure, proper database connectivity, session management, and secure password handling.

🚀 Features

User Registration with secure password hashing (SHA-256)

User Login with session management

Deposit money into account

Withdraw money with balance validation

View current account balance

Clean Maven-based WAR deployment

MySQL database integration

Proper Git hygiene with .gitignore

🛠 Tech Stack

Java: 17

Web Technology: Servlets, HTML, CSS, JavaScript

Build Tool: Maven

Server: Apache Tomcat 9

Database: MySQL 8

JDBC Driver: MySQL Connector/J

📁 Project Structure
banking-system/
│
├── src/
│   └── main/
│       ├── java/
│       │   ├── util/
│       │   │   ├── DBConnection.java
│       │   │   └── PasswordUtils.java
│       │   └── web/
│       │       ├── LoginServlet.java
│       │       ├── RegisterServlet.java
│       │       ├── DepositServlet.java
│       │       ├── WithdrawServlet.java
│       │       └── BalanceServlet.java
│       │
│       ├── resources/
│       │   └── config.properties
│       │
│       └── webapp/
│           ├── login.html
│           ├── register.html
│           ├── dashboard.html
│           └── WEB-INF/
│               └── web.xml
│
├── pom.xml
├── .gitignore
└── README.md


🗄 Database Schema
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50),
    email VARCHAR(50) UNIQUE,
    balance DOUBLE,
    role VARCHAR(12),
    password VARCHAR(256)
);

⚙ Configuration

Create the file below:

src/main/resources/config.properties

db.url=jdbc:mysql://localhost:3306/banking_system?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=root
db.password=YOUR_PASSWORD

▶ How to Run the Project
1️⃣ Prerequisites

Java 17 installed

Maven installed

MySQL running

Apache Tomcat 9 installed

2️⃣ Build the Project
mvn clean package

3️⃣ Deploy to Tomcat

Copy the WAR file:

target/banking-system-1.0.war


to:

apache-tomcat-9/webapps/


Start Tomcat:

startup.bat

4️⃣ Access the Application
http://localhost:8080/banking-system-1.0/login.html

🌐 Application URLs
URL	Description
/login.html	User login
/register.html	User registration
/dashboard.html	User dashboard
/login	Login servlet (POST)
/register	Register servlet (POST)
/deposit	Deposit servlet (POST)
/withdraw	Withdraw servlet (POST)
/balance	View balance (GET)
🔐 Security Notes

Passwords are stored using SHA-256 hashing

Database credentials are externalized

Sessions are used to prevent unauthorized access

Build artifacts and sensitive files are excluded using .gitignore

📌 Key Learnings

Maven-based Java web application structure

Servlet lifecycle and request handling

JDBC database connectivity

Session management

Proper Git version control practices

Debugging real-world encoding and deployment issues

📈 Future Enhancements

Transaction history table

JSP-based UI rendering

DAO & Service layer refactoring

REST API conversion

Input validation & exception handling improvements

👤 Author

Manikanta
Java Full-Stack Developer (Fresher)
Focused on clean architecture and interview-ready projects

⭐ If you like this project

Feel free to ⭐ star the repository and explore further improvements!
## Example
<img width="771" height="476" alt="Screenshot 2025-09-21 164233" src="https://github.com/user-attachments/assets/3fbae90c-38d6-40a7-8192-8f11afd6e073" />

<img width="764" height="250" alt="image" src="https://github.com/user-attachments/assets/d9710af5-9bea-4adc-a8f3-e3c224de319a" />

