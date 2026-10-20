-- Leave Management System v2 schema (MySQL 8; the tests run it on H2 in MySQL mode).
-- v1 stored employees as key/value rows in `employee_meta`; v2 uses ordinary columns and foreign keys.

CREATE TABLE departments (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(100) NOT NULL UNIQUE,
  description VARCHAR(255)
);

CREATE TABLE designations (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(100) NOT NULL UNIQUE,
  description VARCHAR(255)
);

CREATE TABLE leave_types (
  id             INT AUTO_INCREMENT PRIMARY KEY,
  code           VARCHAR(10)  NOT NULL UNIQUE,
  name           VARCHAR(100) NOT NULL,
  description    VARCHAR(255),
  default_days   DECIMAL(5,1),            -- NULL means unlimited (e.g. unpaid leave)
  active         BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE users (
  id                   INT AUTO_INCREMENT PRIMARY KEY,
  username             VARCHAR(50)  NOT NULL UNIQUE,
  password_hash        VARCHAR(100) NOT NULL,             -- BCrypt
  must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
  role                 VARCHAR(10)  NOT NULL,             -- ADMIN, HR, EMPLOYEE
  employee_no          VARCHAR(20),
  first_name           VARCHAR(100) NOT NULL,
  last_name            VARCHAR(100) NOT NULL,
  email                VARCHAR(150),
  department_id        INT,
  designation_id       INT,
  approver             BOOLEAN NOT NULL DEFAULT FALSE,    -- may approve leave for their department
  active               BOOLEAN NOT NULL DEFAULT TRUE,
  last_login           TIMESTAMP NULL,
  created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_users_department  FOREIGN KEY (department_id)  REFERENCES departments(id),
  CONSTRAINT fk_users_designation FOREIGN KEY (designation_id) REFERENCES designations(id),
  CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'HR', 'EMPLOYEE'))
);

-- Days of each leave type an employee gets per calendar year (overrides the type's default).
CREATE TABLE leave_entitlements (
  user_id       INT NOT NULL,
  leave_type_id INT NOT NULL,
  days          DECIMAL(5,1) NOT NULL,
  PRIMARY KEY (user_id, leave_type_id),
  CONSTRAINT fk_ent_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_ent_type FOREIGN KEY (leave_type_id) REFERENCES leave_types(id)
);

CREATE TABLE public_holidays (
  holiday_date DATE PRIMARY KEY,
  name         VARCHAR(100) NOT NULL
);

CREATE TABLE leave_applications (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  user_id       INT NOT NULL,
  leave_type_id INT NOT NULL,
  start_date    DATE NOT NULL,
  end_date      DATE NOT NULL,
  half_day      BOOLEAN NOT NULL DEFAULT FALSE,
  days          DECIMAL(5,1) NOT NULL,
  reason        VARCHAR(500) NOT NULL,
  status        VARCHAR(10) NOT NULL DEFAULT 'PENDING',   -- PENDING, APPROVED, REJECTED, CANCELLED
  decided_by    INT,
  decided_at    TIMESTAMP NULL,
  decision_note VARCHAR(500),
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_app_user    FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_app_type    FOREIGN KEY (leave_type_id) REFERENCES leave_types(id),
  CONSTRAINT fk_app_decider FOREIGN KEY (decided_by) REFERENCES users(id),
  CONSTRAINT ck_app_status  CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
  CONSTRAINT ck_app_dates   CHECK (end_date >= start_date)
);

CREATE INDEX idx_app_user_status ON leave_applications (user_id, status);
