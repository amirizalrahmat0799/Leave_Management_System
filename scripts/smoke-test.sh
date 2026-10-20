#!/usr/bin/env bash
# End-to-end check against a running app (docker compose up): sign in, apply for leave, approve it,
# and confirm the v1 holes are closed. Usage: scripts/smoke-test.sh [base-url]
set -euo pipefail
BASE="${1:-http://localhost:8080}"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

fail() { echo "FAIL: $*" >&2; exit 1; }
pass() { echo "ok   $*"; }

# curl with a cookie jar per user
req() { local who="$1"; shift; curl -sS -b "$WORK/$who.jar" -c "$WORK/$who.jar" "$@"; }
# the one-time form token from a page
token() { sed -n 's/.*name="token" value="\([^"]*\)".*/\1/p' | head -1; }

for i in $(seq 1 60); do
  curl -sf -o /dev/null "$BASE/login.action" && break
  sleep 2
done

# 1. The v1 login could be bypassed with SQL injection; v2 must refuse it
req attacker -o /dev/null "$BASE/login.action"
req attacker -o "$WORK/inj.html" --data-urlencode "username=admin' -- " --data-urlencode "password=x" "$BASE/login.action"
grep -q "Wrong username or password" "$WORK/inj.html" || fail "SQL injection login was not refused"
pass "SQL injection in the login form is refused"

# 2. Sign in as an employee and open the dashboard
req hafiz -o /dev/null "$BASE/login.action"
req hafiz -L -o "$WORK/dash.html" --data "username=hafiz&password=demo1234" "$BASE/login.action"
grep -q "Hello, Hafiz" "$WORK/dash.html" || fail "hafiz could not sign in"
pass "employee signs in"

# 3. Employees can't open HR pages
req hafiz -o "$WORK/hr.html" "$BASE/hr/employees.action"
grep -q "Not allowed" "$WORK/hr.html" || fail "employee reached an HR page"
pass "HR pages are closed to employees"

# 4. Apply for leave (with the form token), then submitting the same form again is ignored
TOKEN="$(req hafiz "$BASE/apply.action" | token)"
[ -n "$TOKEN" ] || fail "no form token on the apply page"
START="$(date -d 'next monday + 28 days' +%Y-%m-%d)"
END="$(date -d "$START + 1 day" +%Y-%m-%d)"
FORM="struts.token.name=token&token=$TOKEN&leaveTypeId=1&startDate=$START&endDate=$END&reason=Smoke+test+trip"
req hafiz -L -o "$WORK/applied.html" --data "$FORM" "$BASE/apply-submit.action"
grep -q "Smoke test trip" "$WORK/applied.html" || fail "the application was not saved"
pass "employee applies for 2 days of annual leave"
req hafiz -L -o /dev/null --data "$FORM" "$BASE/apply-submit.action"
COUNT="$(req hafiz "$BASE/my-leave.action?year=${START:0:4}" | grep -c "Smoke test trip" || true)"
[ "$COUNT" = "1" ] || fail "a resubmitted form created a second application ($COUNT)"
pass "a resubmitted form is ignored"

# 5. Daniel (IT approver) approves it
req daniel -o /dev/null "$BASE/login.action"
req daniel -L -o /dev/null --data "username=daniel&password=demo1234" "$BASE/login.action"
ID="$(req daniel "$BASE/review.action" | sed -n 's/.*review-view.action?id=\([0-9]*\).*/\1/p' | sort -n | tail -1)"
[ -n "$ID" ] || fail "daniel sees no applications to review"
TOKEN="$(req daniel "$BASE/review-view.action?id=$ID" | token)"
req daniel -L -o "$WORK/decided.html" --data "struts.token.name=token&token=$TOKEN&id=$ID&decision=approve" "$BASE/review-decide.action"
grep -q "Application approved" "$WORK/decided.html" || fail "daniel could not approve"
pass "department approver approves it"

# 6. The default admin must change the password before doing anything else
req admin -o /dev/null "$BASE/login.action"
req admin -L -o "$WORK/admin.html" --data "username=admin&password=admin123" "$BASE/login.action"
grep -q "Change your password" "$WORK/admin.html" || fail "admin was not asked to change the default password"
pass "default admin password must be changed first"

echo "All smoke tests passed."
