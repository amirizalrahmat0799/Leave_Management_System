# Leave Management System

[![CI](https://github.com/amirizalrahmat0799/Leave_Management_System/actions/workflows/ci.yml/badge.svg)](https://github.com/amirizalrahmat0799/Leave_Management_System/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-17-blue)
![Struts](https://img.shields.io/badge/Apache_Struts-7.4-6a1b9a)
![Tomcat](https://img.shields.io/badge/Tomcat-10.1-f8dc75)
![MySQL](https://img.shields.io/badge/MySQL-8.4-00758f)

Staff apply for leave, their department approver or HR approves it, and everyone can see what's left.

This is **version 2** of my final-year project. Version 1 was written in PHP with CodeIgniter and is kept as the
[`v1.0.0-php`](https://github.com/amirizalrahmat0799/Leave_Management_System/tree/v1.0.0-php) tag. Version 2 rebuilds
it as a classic Java enterprise web app, the kind many banks and government agencies still run: **Apache Struts 2
actions and JSPs on Tomcat, plain JDBC, MySQL**. It's "legacy" in style only: it runs on the current, supported
Struts 7 and fixes the security problems version 1 had.

## Features

- **One sign-in for everyone**, with three roles: employees, HR and administrators. Employees can also be
  **approvers** for their department.
- **Apply for leave:** whole days or a half day. Only working days count: weekends and public holidays are skipped.
  Sick leave can be applied for up to 30 days afterwards.
- **Balances per year:** each leave type has a default number of days, which HR can change for each employee.
  Pending applications already count, so nobody can apply for more than they have. Unpaid leave is unlimited.
- **Approvals:** HR and admins decide for everyone; approvers for their own department. Nobody approves their own
  leave, a rejection needs a reason, and if two people decide at the same moment, only the first one counts.
- **Cancelling:** employees withdraw pending applications, and approved ones that haven't started yet.
- **HR pages:** employees, departments, designations, leave types, public holidays, and a report of approved
  leave per department and month with a CSV download.
- **Dashboard:** your balances, applications waiting for you, who's on leave today, and the next public holidays.

## What changed from version 1

| | Version 1 (PHP) | Version 2 (Java) |
|---|---|---|
| Login query | Built by pasting the form fields into the SQL, so `admin' -- ` logged in as admin without a password | Every query is a `PreparedStatement`; a test and the CI smoke test try the old attack |
| Passwords | Unsalted MD5 | BCrypt (cost 12); the default admin must choose a new password at first sign-in |
| Guessing passwords | Unlimited tries | Locked for 5 minutes after 5 wrong passwords |
| Forms | No protection against double submits or cross-site requests | A one-time token on every form that changes data (Struts token interceptor) |
| Sessions | Same session id before and after login | New session id at login (no session fixation); HTTP-only, SameSite cookies |
| Hidden accounts | An unused developer account with its password hash in `initialize.php` | None |
| Employee data | Key/value rows (`employee_meta`) and leave credits stored as JSON text | Ordinary tables with foreign keys and check constraints |
| Leave days | Calendar days | Working days, minus public holidays |
| Database password | In the source code | From environment variables, through Tomcat's connection pool |
| Tests | None | JUnit tests for the leave rules, sign-in and data access; an end-to-end test on Tomcat and MySQL in CI |

## Run it

With Docker:

```bash
docker compose up --build
```

Then open <http://localhost:8080>. This starts MySQL 8.4 with the sample data and the app on Tomcat 10.1.

| User | Password | Role |
|---|---|---|
| `admin` | `admin123` | Administrator (asked to change the password first) |
| `aina` | `demo1234` | HR |
| `daniel` | `demo1234` | IT manager, approves for the IT department |
| `hafiz` | `demo1234` | IT software engineer |
| `priya` | `demo1234` | Finance executive |

All the people in the sample data are fictional.

### Without Docker

1. Create a MySQL database and run `src/main/resources/db/schema.sql`, then `sample-data.sql`.
2. Copy the MySQL driver (`mysql-connector-j`) into Tomcat 10.1's `lib` folder.
3. Set the connection in Tomcat's `bin/setenv.sh` (or `setenv.bat`), as in `docker/tomcat/setenv.sh`:
   `-Dleave.db.url=jdbc:mysql://localhost:3306/leave_db -Dleave.db.user=... -Dleave.db.password=...`
4. `mvn package` and deploy `target/leave.war`.

In IntelliJ IDEA, add a Tomcat 10.1 run configuration that deploys the project's "war exploded" artifact.

## How it's built

```
Browser ──> Tomcat 10.1 ──> StrutsPrepareAndExecuteFilter
                              │  interceptors: auth ─> role (HR pages) ─> token (forms) ─> parameters ...
                              v
                            Action (web/action) ──> Services: LeaveService, AuthService
                              │                         │
                              v                         v
                            JSP (WEB-INF/jsp)        DAOs (plain JDBC) ──> jdbc/leave pool (context.xml) ──> MySQL
```

- **Struts 2 the classic way:** actions and results declared in `struts.xml`, interceptor stacks for signed-in
  pages, HR pages and form submissions, and JSPs with Struts and JSTL tags. Struts 7's newer protections are on:
  only setters marked `@StrutsParameter` accept request values, and the JSP tags may only read this app's own classes.
- **No framework for the rest:** services and DAOs are wired by hand at startup (`AppStartup`), and the DAOs are
  plain JDBC on Tomcat's connection pool.
- **The leave rules live in one class,** `LeaveService`, which the tests exercise directly.

```
src/main/java/com/ahmadamirizal/leave
  model/        users, leave types, applications, balances
  dao/          JDBC data access
  service/      leave rules, sign-in, login throttling
  web/          startup, base action, interceptors
  web/action/   the Struts actions
src/main/resources   struts.xml, schema and sample data
src/main/webapp      JSPs, CSS, web.xml, context.xml
```

## Tests

```bash
mvn verify                          # unit and data-access tests (H2 in MySQL mode)
docker compose up -d --build --wait
scripts/smoke-test.sh               # end to end on Tomcat and MySQL
```

- **Leave rules:** working days and holidays, half days, balances with per-employee entitlements, overlaps,
  backdating, crossing into a new year, unlimited unpaid leave, who may decide, deciding only once, cancelling.
- **Sign-in:** right and wrong passwords, inactive accounts, the version 1 SQL injection, BCrypt hashes,
  changing and resetting passwords.
- **End to end:** signing in, HR pages closed to employees, applying with a form token, a resubmitted form being
  ignored, a department approver approving, and the default admin password having to be changed.

## Roadmap

- [ ] Email to the approver when someone applies, and to the employee when it's decided
- [ ] Carry unused annual leave into the next year, up to a limit
- [ ] Holidays per state (Malaysian states have different public holidays)
- [ ] Migrate to Spring Boot, as a documented step-by-step migration

## License

[MIT](LICENSE) © Ahmad Amirizal Rahmat. All people in the sample data are fictional.
