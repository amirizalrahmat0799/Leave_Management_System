-- Sample data: fictional people only.
-- Logins: admin / admin123 (must be changed at first login); everyone else's password is demo1234.

INSERT INTO departments (id, name, description) VALUES
  (1, 'Human Resources', 'People and payroll'),
  (2, 'Information Technology', 'Systems and support'),
  (3, 'Finance', 'Accounts and finance');

INSERT INTO designations (id, name, description) VALUES
  (1, 'Executive', 'Office executive'),
  (2, 'Software Engineer', 'Builds and maintains internal systems'),
  (3, 'Manager', 'Leads a department'),
  (4, 'HR Executive', 'Human resources executive');

INSERT INTO leave_types (id, code, name, description, default_days, active) VALUES
  (1, 'AL', 'Annual Leave', 'Paid annual leave', 14.0, TRUE),
  (2, 'SL', 'Sick Leave', 'Paid sick leave with a medical certificate', 14.0, TRUE),
  (3, 'EL', 'Emergency Leave', 'Short-notice leave for emergencies', 3.0, TRUE),
  (4, 'UL', 'Unpaid Leave', 'Leave without pay', NULL, TRUE);

INSERT INTO users (id, username, password_hash, must_change_password, role, employee_no, first_name, last_name, email,
                   department_id, designation_id, approver) VALUES
  (1, 'admin', '$2a$10$As846IF4adN4v9u9QAmrReZxy3APfYb0aqLLKPTN8thzurQgP0XLm', TRUE, 'ADMIN', NULL,
      'System', 'Administrator', NULL, NULL, NULL, FALSE),
  (2, 'aina', '$2a$10$.ewWBSTD.kXvOQZUaf6CCONwZmtykS1Whcrl0.l/BlGnU8104mw1W', FALSE, 'HR', 'E1001',
      'Nur Aina', 'Rahman', 'aina@example.com', 1, 4, TRUE),
  (3, 'daniel', '$2a$10$.ewWBSTD.kXvOQZUaf6CCONwZmtykS1Whcrl0.l/BlGnU8104mw1W', FALSE, 'EMPLOYEE', 'E2001',
      'Daniel', 'Lim', 'daniel@example.com', 2, 3, TRUE),
  (4, 'hafiz', '$2a$10$.ewWBSTD.kXvOQZUaf6CCONwZmtykS1Whcrl0.l/BlGnU8104mw1W', FALSE, 'EMPLOYEE', 'E2002',
      'Hafiz', 'Ismail', 'hafiz@example.com', 2, 2, FALSE),
  (5, 'priya', '$2a$10$.ewWBSTD.kXvOQZUaf6CCONwZmtykS1Whcrl0.l/BlGnU8104mw1W', FALSE, 'EMPLOYEE', 'E3001',
      'Priya', 'Nair', 'priya@example.com', 3, 1, FALSE);

-- Hafiz gets two extra days of annual leave; everyone else uses the defaults.
INSERT INTO leave_entitlements (user_id, leave_type_id, days) VALUES (4, 1, 16.0);

-- Fixed-date national holidays. Add the moving ones (Hari Raya, Chinese New Year, Deepavali...) under Holidays.
INSERT INTO public_holidays (holiday_date, name) VALUES
  ('2026-01-01', 'New Year''s Day'),
  ('2026-05-01', 'Labour Day'),
  ('2026-06-01', 'Agong''s Birthday'),
  ('2026-08-31', 'National Day'),
  ('2026-09-16', 'Malaysia Day'),
  ('2026-12-25', 'Christmas Day');

INSERT INTO leave_applications (user_id, leave_type_id, start_date, end_date, half_day, days, reason, status,
                                decided_by, decided_at) VALUES
  (4, 1, '2026-09-14', '2026-09-18', FALSE, 4.0, 'Family trip to Langkawi', 'APPROVED', 3, '2026-09-01 10:00:00'),
  (4, 2, '2026-10-05', '2026-10-05', FALSE, 1.0, 'Fever', 'APPROVED', 3, '2026-10-05 09:12:00'),
  (5, 1, '2026-12-21', '2026-12-24', FALSE, 4.0, 'Year-end break', 'PENDING', NULL, NULL),
  (4, 3, '2026-11-02', '2026-11-02', TRUE, 0.5, 'Car service appointment', 'PENDING', NULL, NULL);
