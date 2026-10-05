# SkillBridge

SkillBridge is a Spring Boot platform for peer-to-peer teaching and learning. Members can list skills to teach and learn, discover mutual matches, exchange requests, schedule online classes, and share opportunities within skill communities.

## Features

- Member registration and login with hashed passwords
- Skill profiles, mentor directory, and mutual skill matching
- Skill exchange requests and mentor-scheduled Google Meet classes
- Per-skill communities for news, jobs, internships, and freelance opportunities
- Shared learning resources, assignments, and progress tracking

## Requirements

- Java 21 or newer
- MySQL 8 with a `skillbridge_db` database

## Run locally

Set the datasource environment variables, then start Spring Boot:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/skillbridge_db"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your-local-password"
.\mvnw.cmd spring-boot:run
```

Open `http://localhost:8080`. The root `.env.example` lists the variable names; Spring Boot does not automatically load `.env` files.

Unless the `prod` profile is active, startup seeds starter skills and demo members until there are at least 50 accounts. Demo login: `demo.member01@skillbridge.demo` through `demo.member50@skillbridge.demo`, password `SkillBridgeDemo2026!`.

For online classes, the mentor schedules a future time and supplies an existing Google Meet URL. The app does not create Meet links through Google Calendar.

## Tests

Configure the same database environment variables, then run:

```powershell
.\mvnw.cmd test
```