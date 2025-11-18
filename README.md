# Banking Management System (Console-Based)

## Features
- User registration & login
- Change password & account locking on failed attempts
- View account balance & transaction history
- Mini-statement & transaction filtering
- Admin functions: view users, block user, reset balance, credit monthly interest
- Savings account interest calculation (4% yearly, monthly update)

## Setup Instructions
1. Install MySQL and create a database `banking_system`.
2. Import the provided `schema.sql` for `users`, `accounts`, and `transactions` tables.
3. Configure `DBConnection.java` with your MySQL credentials.
4. Ensure `JDBC` driver is added to project dependencies.

## How to Run
1. Compile all `.java` files:  
   ```bash
   javac *.java

2. Run the main class:
    ```bash
   java Main

## Example
<img width="771" height="476" alt="Screenshot 2025-09-21 164233" src="https://github.com/user-attachments/assets/3fbae90c-38d6-40a7-8192-8f11afd6e073" />

<img width="764" height="250" alt="image" src="https://github.com/user-attachments/assets/d9710af5-9bea-4adc-a8f3-e3c224de319a" />

