# API Test Guide — UC-13 through UC-24

Base URL: `http://localhost:8080`

All responses follow this envelope:
```json
{
  "flag": true,
  "code": 200,
  "message": "...",
  "data": { ... }
}
```

---

## Seed Data (loaded on startup)

| Type | ID (check H2 console or GET list endpoints) | Details |
|------|----------------------------------------------|---------|
| Section | auto | name = "2024-2025" |
| Section | auto | name = "2023-2024" |
| Instructor | auto | Alice Smith — ACTIVE, on Team Alpha & Gamma |
| Instructor | auto | Robert Johnson — ACTIVE, on Team Beta |
| Instructor | auto | Linda Lee — ACTIVE, on Team Beta |
| Instructor | auto | Mark Brown — DEACTIVATED, on no team |
| Team | auto | Team Alpha — section 2024-2025, students: Alice Adams, Bob Baker, Carol Clark |
| Team | auto | Team Beta — section 2024-2025, students: David Davis, Eve Evans, Frank Foster |
| Team | auto | Team Gamma — section 2023-2024, students: Grace Green, Henry Harris |
| Team | auto | Team Delta — section 2024-2025, no students or instructors |
| Student | auto | Alice Adams — Team Alpha, 3 WARs, 2 Peer Evals |
| Student | auto | Bob Baker, Carol Clark, David Davis, Eve Evans, Frank Foster — Team Alpha/Beta |
| Student | auto | Grace Green, Henry Harris — Team Gamma |

> Use `GET /api/students` and `GET /api/instructors` to find the auto-generated IDs.

---

## UC-15 — Find Students

**GET** `/api/students`

Find all students. All query parameters are optional and can be combined.

**No filter (returns all):**
```
GET http://localhost:8080/api/students
```

**Filter by first name:**
```
GET http://localhost:8080/api/students?firstName=Alice
```

**Filter by last name:**
```
GET http://localhost:8080/api/students?lastName=Baker
```

**Filter by email:**
```
GET http://localhost:8080/api/students?email=tcu.edu
```

**Filter by section name:**
```
GET http://localhost:8080/api/students?sectionName=2024-2025
```

**Filter by team name:**
```
GET http://localhost:8080/api/students?teamName=Alpha
```

**Combine filters:**
```
GET http://localhost:8080/api/students?sectionName=2024-2025&lastName=Adams
```

**Expected response (200):**
```json
{
  "flag": true,
  "code": 200,
  "message": "Success",
  "data": [
    { "id": 1, "firstName": "Alice", "lastName": "Adams", "teamName": "Team Alpha" }
  ]
}
```

**No match (200 with empty list):**
```
GET http://localhost:8080/api/students?firstName=Nobody
```
```json
{ "flag": true, "code": 200, "message": "Success", "data": [] }
```

---

## UC-16 — View a Student

**GET** `/api/students/{id}`

Returns full student details including WAR count and peer evaluation count.

```
GET http://localhost:8080/api/students/1
```

**Expected response (200):**
```json
{
  "flag": true,
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "firstName": "Alice",
    "lastName": "Adams",
    "email": "a.adams@tcu.edu",
    "sectionName": "2024-2025",
    "teamNames": ["Team Alpha"],
    "warCount": 3,
    "peerEvaluationCount": 2
  }
}
```

**Not found (404):**
```
GET http://localhost:8080/api/students/9999
```
```json
{ "flag": false, "code": 404, "message": "Student not found with id: 9999", "data": null }
```

---

## UC-13 — Remove a Student from a Team

**DELETE** `/api/teams/{teamId}/students/{studentId}`

Removes the student from the team without deleting the student.

```
DELETE http://localhost:8080/api/teams/1/students/2
```
*(Replace 1 and 2 with actual team ID and student ID)*

**Expected response (200):**
```json
{
  "flag": true,
  "code": 200,
  "message": "Student removed from team successfully",
  "data": null
}
```

**Student not on that team (400):**
```json
{ "flag": false, "code": 400, "message": "Student X is not a member of team Y", "data": null }
```

---

## UC-17 — Delete a Student

**DELETE** `/api/students/{id}`

Permanently deletes the student and all associated WARs and peer evaluations.

```
DELETE http://localhost:8080/api/students/1
```

**Expected response (200):**
```json
{
  "flag": true,
  "code": 200,
  "message": "Student deleted successfully",
  "data": null
}
```

**Not found (404):**
```json
{ "flag": false, "code": 404, "message": "Student not found with id: 9999", "data": null }
```

---

## UC-21 — Find Instructors

**GET** `/api/instructors`

All parameters are optional and can be combined.

**No filter (returns all):**
```
GET http://localhost:8080/api/instructors
```

**Filter by status (ACTIVE or DEACTIVATED):**
```
GET http://localhost:8080/api/instructors?status=ACTIVE
GET http://localhost:8080/api/instructors?status=DEACTIVATED
```

**Filter by last name:**
```
GET http://localhost:8080/api/instructors?lastName=Smith
```

**Filter by team name:**
```
GET http://localhost:8080/api/instructors?teamName=Alpha
```

**Expected response (200):**
```json
{
  "flag": true,
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "firstName": "Alice",
      "lastName": "Smith",
      "teamNames": ["Team Alpha", "Team Gamma"],
      "status": "ACTIVE"
    }
  ]
}
```

---

## UC-22 — View an Instructor

**GET** `/api/instructors/{id}`

Returns full instructor details with teams grouped by section.

```
GET http://localhost:8080/api/instructors/1
```

**Expected response (200):**
```json
{
  "flag": true,
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "firstName": "Alice",
    "lastName": "Smith",
    "email": "a.smith@tcu.edu",
    "status": "ACTIVE",
    "supervisedTeamsBySection": {
      "2024-2025": ["Team Alpha"],
      "2023-2024": ["Team Gamma"]
    }
  }
}
```

**Not found (404):**
```json
{ "flag": false, "code": 404, "message": "Instructor not found with id: 9999", "data": null }
```

---

## UC-18 — Invite Instructors (generate registration links)

**POST** `/api/instructors/invite`

Returns unique registration links to share manually. No email is sent.

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "emails": ["new.prof@tcu.edu", "another.prof@tcu.edu"]
}
```

**Request:**
```
POST http://localhost:8080/api/instructors/invite
```

**Expected response (200):**
```json
{
  "flag": true,
  "code": 200,
  "message": "Invitation links generated. Share these links with the instructors.",
  "data": [
    {
      "email": "new.prof@tcu.edu",
      "inviteLink": "http://localhost:8080/api/instructors/register?token=550e8400-e29b-41d4-a716-446655440000"
    },
    {
      "email": "another.prof@tcu.edu",
      "inviteLink": "http://localhost:8080/api/instructors/register?token=7f6d6c5b-..."
    }
  ]
}
```

**Empty emails (400):**
```json
{ "flag": false, "code": 400, "message": "At least one email is required", "data": null }
```

---

## UC-19 — Assign Instructors to a Team

**PUT** `/api/teams/{teamId}/instructors`

Assigns one or more instructors to a team (additive — does not replace existing assignments).

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "instructorIds": [1, 2]
}
```

**Request (assign to Team Delta, which starts empty):**
```
PUT http://localhost:8080/api/teams/4/instructors
```

**Expected response (200):**
```json
{
  "flag": true,
  "code": 200,
  "message": "Instructors assigned to team successfully",
  "data": {
    "id": 4,
    "name": "Team Delta",
    "sectionName": "2024-2025",
    "studentNames": [],
    "instructorNames": ["Alice Smith", "Robert Johnson"]
  }
}
```

**Invalid instructor ID (400):**
```json
{ "flag": false, "code": 400, "message": "One or more instructor IDs not found", "data": null }
```

---

## UC-20 — Remove an Instructor from a Team

**DELETE** `/api/teams/{teamId}/instructors/{instructorId}`

Removes the instructor from the team without deleting the instructor account.

```
DELETE http://localhost:8080/api/teams/1/instructors/1
```
*(Remove Alice Smith from Team Alpha)*

**Expected response (200):**
```json
{
  "flag": true,
  "code": 200,
  "message": "Instructor removed from team successfully",
  "data": {
    "id": 1,
    "name": "Team Alpha",
    "sectionName": "2024-2025",
    "studentNames": ["Alice Adams", "Bob Baker", "Carol Clark"],
    "instructorNames": []
  }
}
```

**Instructor not on that team (400):**
```json
{ "flag": false, "code": 400, "message": "Instructor X is not assigned to team Y", "data": null }
```

---

## UC-14 — Delete a Team

**DELETE** `/api/teams/{teamId}`

Permanently deletes the team and removes all student/instructor associations. Does NOT delete the students or instructors themselves.

```
DELETE http://localhost:8080/api/teams/4
```
*(Delete Team Delta)*

**Expected response (200):**
```json
{
  "flag": true,
  "code": 200,
  "message": "Team deleted successfully",
  "data": null
}
```

**Not found (404):**
```json
{ "flag": false, "code": 404, "message": "Team not found with id: 9999", "data": null }
```

---

## UC-23 — Deactivate an Instructor

**PUT** `/api/instructors/{id}/deactivate`

Sets the instructor's status to DEACTIVATED. Their account data is preserved.

```
PUT http://localhost:8080/api/instructors/2/deactivate
```
*(Deactivate Robert Johnson)*

**Expected response (200):**
```json
{
  "flag": true,
  "code": 200,
  "message": "Instructor deactivated successfully",
  "data": {
    "id": 2,
    "firstName": "Robert",
    "lastName": "Johnson",
    "email": "r.johnson@tcu.edu",
    "status": "DEACTIVATED",
    "supervisedTeamsBySection": { "2024-2025": ["Team Beta"] }
  }
}
```

**Already deactivated (400):**
```json
{ "flag": false, "code": 400, "message": "Instructor 4 is already deactivated", "data": null }
```

---

## UC-24 — Reactivate an Instructor

**PUT** `/api/instructors/{id}/reactivate`

Sets the instructor's status back to ACTIVE.

```
PUT http://localhost:8080/api/instructors/4/reactivate
```
*(Reactivate Mark Brown)*

**Expected response (200):**
```json
{
  "flag": true,
  "code": 200,
  "message": "Instructor reactivated successfully",
  "data": {
    "id": 4,
    "firstName": "Mark",
    "lastName": "Brown",
    "email": "m.brown@tcu.edu",
    "status": "ACTIVE",
    "supervisedTeamsBySection": {}
  }
}
```

**Already active (400):**
```json
{ "flag": false, "code": 400, "message": "Instructor 1 is already active", "data": null }
```

---

## H2 Console

While the app is running, open the database browser at:

```
http://localhost:8080/h2-console
```

Settings:
- JDBC URL: `jdbc:h2:mem:projectpulse`
- Username: `sa`
- Password: *(leave blank)*

Useful queries:
```sql
SELECT * FROM SECTIONS;
SELECT * FROM TEAMS;
SELECT * FROM STUDENTS;
SELECT * FROM INSTRUCTORS;
SELECT * FROM TEAM_STUDENT;
SELECT * FROM TEAM_INSTRUCTOR;
SELECT * FROM WARS;
SELECT * FROM PEER_EVALUATIONS;
SELECT * FROM INVITATION_TOKENS;
```

---

## Suggested Postman Test Order

1. `GET /api/students` — note the IDs
2. `GET /api/instructors` — note the IDs
3. `GET /api/students/1` — view Alice's details (3 WARs, 2 evals)
4. `GET /api/instructors/1` — view Alice Smith's teams by section
5. `GET /api/instructors?status=DEACTIVATED` — should return Mark Brown only
6. `POST /api/instructors/invite` — generate an invite link
7. `PUT /api/teams/4/instructors` — assign instructors to empty Team Delta
8. `GET /api/instructors/1` — confirm Alice Smith now on Team Delta too
9. `DELETE /api/teams/4/instructors/{instructorId}` — remove from Team Delta
10. `DELETE /api/teams/1/students/{studentId}` — remove a student from Team Alpha
11. `PUT /api/instructors/2/deactivate` — deactivate Robert Johnson
12. `GET /api/instructors?status=ACTIVE` — confirm Johnson no longer in active list
13. `PUT /api/instructors/2/reactivate` — reactivate Johnson
14. `DELETE /api/students/{id}` — delete a student (verify they disappear from GET list)
15. `DELETE /api/teams/4` — delete Team Delta

> **Note:** H2 is in-memory — all data resets when the server restarts.
