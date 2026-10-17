# TenantGuard Lab API

All routes start with `/api`. Registration and login are public; all other routes require `Authorization: Bearer <JWT>`. The JWT subject identifies the user. Roles belong to a membership in one organization, not to the user globally.

| Operation | Route | Minimum role | Success |
| --- | --- | --- | --- |
| Register | `POST /auth/register` | Public | 201 |
| Login | `POST /auth/login` | Public | 200 |
| Create organization | `POST /organizations` | Authenticated | 201; creator becomes ADMIN |
| Create document | `POST /organizations/{organizationId}/documents` | EDITOR | 201 |
| List documents | `GET /organizations/{organizationId}/documents` | VIEWER | 200 |
| Read document | `GET /organizations/{organizationId}/documents/{documentId}` | VIEWER | 200 |
| Replace document title and content | `PUT /organizations/{organizationId}/documents/{documentId}` | EDITOR | 200 |
| Delete document | `DELETE /organizations/{organizationId}/documents/{documentId}` | EDITOR | 204 |
| Add member | `POST /organizations/{organizationId}/members` | ADMIN | 201 |
| List members | `GET /organizations/{organizationId}/members` | ADMIN | 200 |
| Change member role | `PUT /organizations/{organizationId}/members/{memberId}/role` | ADMIN | 200 |
| Remove member | `DELETE /organizations/{organizationId}/members/{memberId}` | ADMIN | 204 |

Role hierarchy: ADMIN includes EDITOR and VIEWER permissions; EDITOR includes VIEWER permissions. Document edits do not change `createdBy`. `memberId` is the membership ID returned by add/list, not the user ID. Adding a member requires an existing username and a role of `ADMIN`, `EDITOR`, or `VIEWER`. Changing a role requires a JSON body such as `{"role":"EDITOR"}`. Document creation and replacement use `{"title":"...","content":"..."}`. Both fields are required; title must be nonblank and at most 150 characters.

Every document and membership lookup uses both the organization ID in the route and the resource ID. A user without a membership gets 403. A member with insufficient role gets 403. An authorized member requesting a resource outside that organization gets 404. The API rejects role changes or removals that would leave an organization without an ADMIN with 409, including self-demotion and self-removal. Membership modifications acquire an organization row lock before checking and changing membership, so concurrent changes cannot both remove the last administrator.

Other responses: 400 for invalid JSON, fields, or path parameters; 401 for invalid login or missing/invalid JWT; 404 for a missing document, member, user being added, or organization being modified; 409 for duplicate users, organizations, memberships, or database uniqueness conflicts. Error responses are plain text. No pagination or audit log is implemented in this portfolio API.
