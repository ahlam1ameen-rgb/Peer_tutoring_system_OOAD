[README.md](https://github.com/user-attachments/files/35244865/README.md)
# Peer Tutoring System

A desktop application for Al Ain University (AAU) students to find a peer tutor for a course, book a tutoring session, and rate the tutor afterwards. An admin manages the student and tutor accounts.

**Course:** Object-Oriented Analysis & Design (0103430), Fall 2026, Dr. Yazeed Ghadi
**Team:** Rand Yousef, Aryam Maher, Sara Al Jalal, Ahlam Muaagar

## How to run

You need **Java 17 or later** (JDK).

**From an editor (easiest):** open this folder in VS Code, NetBeans or IntelliJ, open `src/peertutoring/Main.java` and click **Run**.

**From the command line,** inside this folder:

```
javac -d out src/peertutoring/*.java src/peertutoring/model/*.java src/peertutoring/controller/*.java src/peertutoring/data/*.java src/peertutoring/ui/*.java
java -cp out peertutoring.Main
```

## First login

The first time the program runs, it creates a `data` folder with only the admin account:

| University ID | Password | Role |
|---|---|---|
| 900000001 | admin123 | Admin |

Students and tutors create their own accounts with **Create an account** on the Login screen. Everything is saved in the `data` folder; delete it to start again with an empty system.

## Project structure

```
src/peertutoring/
├── Main.java      starts the program
├── model/         User, Student, Tutor, Admin, Course, TimeSlot, Session, SessionStatus, Feedback
├── controller/    AuthController, BookingController, CourseController,
│                  AvailabilityController, FeedbackController, AdminController
├── data/          DataStore (reads and writes the text files)
└── ui/            the screens and dialogs (Swing)
```

The code follows a three-tier architecture: **ui → controller → model**, with **data** handling storage.
