# ELEVON ERP — Gap Closure Log

Tracks the audit-driven fixes from the full-stack gap closure sprint, wave by wave.

## Wave 0 — Baseline audit

Confirmed via direct code reads (not assumptions):
- No `/admin/*` routes exist yet — admin area lives at `/accountants/{users,roles,lookups,screens}`.
- `PermissionGuard` and `PermissionService.can()` exist but were wired to zero routes; only `AuthGuard` (global) and `AdminGuard` (accountants only) guarded routes.
- `AdminGuard`/`PermissionGuard` race condition confirmed: `APP_INITIALIZER` awaited `PermissionService.loadMine()` but not `AuthService.refreshCurrentUser()`, so a hard refresh on `/accountants/*` could deny a legitimate admin before `/profile/me` resolved.
- `MenuActionAuthorizationFilter` mapped `/inventory/movements` but the real controller path is `/inventory/stock/movements` — menu-action enforcement silently skipped for all stock mutation endpoints.
- `must_change_password` column and `screen_settings` table do not exist yet (Wave 2 scope).
- `NotificationService.createForRecipients()` exists but is called from zero places.
- Smoke test script (`api-integration-test.mjs`) had no coverage of admin/access, lookups, notifications, reconciliation, reports, OTP, or any workflow POST beyond exchange-rate CRUD.

## Wave 1 — Critical bug fixes (P0)

### 1.1 Fake delete handlers
Audited `removeItem()` in `budget-page`, `bills-page`, `transactions-page`, `invoices-page`, `checks-page` components. Found the "fake delete" was **dead code**: none of these pages expose a Delete button — each overrides `tableActions` to show only view/edit plus real workflow actions (approve/cancel/deposit/clear/bounce/activate/close), all correctly wired to real backend endpoints. Changed the unreachable `removeItem()` stubs from `of(undefined)` (silent fake success) to `throwError(...)` with a descriptive message, so the dead path fails loudly instead of lying if ever invoked.

### 1.2 AdminGuard/PermissionGuard race
Added `AuthService.initCurrentUser()` which resolves once with the loaded user (or `null`) and is now awaited by `APP_INITIALIZER` via `firstValueFrom`, replacing the previous fire-and-forget `refreshCurrentUser()` call. Angular blocks initial route activation until all `APP_INITIALIZER`s resolve, so guards never evaluate before `/profile/me` completes — fixes hard-refresh denial on `/accountants/*`.

### 1.3 MenuActionAuthorizationFilter path bug
Fixed the path map entry from `/inventory/movements` to `/inventory/stock/movements`, and added `/inventory/stock/in`, `/inventory/stock/out`, `/inventory/stock/transfer` (previously entirely unmapped) — all pointing at the `erp-inventory-movements` menu item.

### 1.4 Bill/Invoice GL posting
Confirmed the backend already treats `/approve` and `/post` as aliases for both bills (`BillService.postBill()` calls `approveBill()`) and invoices (`CustomerInvoiceController` routes both to `postInvoice()`) — the existing "Approve" button already creates the GL journal entry and posts. No redundant "Post" button was added. Instead fixed the real gap: both pages' status filter dropdowns were missing `POSTED`/`PARTIALLY_PAID` (bills) and `POSTED`/`PARTIAL` (invoices), and invoices had a bogus `'APPROVED'` filter option that doesn't exist in the backend `InvoiceStatus` enum.

### 1.5 Stock workflow gaps
- Added `activate*`/`deactivate*` methods to `ErpApiService` for products, categories, warehouses, units (backend endpoints already existed) and wired Activate/Deactivate table actions on all four master-list pages.
- Added `submitStockMovement()` to `ErpApiService` and a "Submit" table action on the movements page (DRAFT → PENDING, optional step before Approve).
- `POST /inventory/stock/in|out|transfer` intentionally left unwired in the UI: they are aliases that call the same `createMovement()` with a preset `movementType` — the existing generic movement-creation form (IN/OUT/TRANSFER/ADJUSTMENT dropdown → `POST /inventory/stock/movements`) already covers this without duplicate UI.
- Added missing `COMMON.ACTIVATE_SUCCESS`/`DEACTIVATE_SUCCESS` and `ERP.SUBMIT`/`SUBMIT_SUCCESS` i18n keys (en/ar).

## Wave 1 verification
- Backend: `mvnw spring-boot:run` — Flyway validated 13 migrations, schema `erp_system` at v12, started cleanly on port 8087. The earlier test-suite stall was subsequently rechecked and is no longer reproducible; see the final verification note below.
- `npm run test:api` — 57/57 passed against the live backend, no regressions.
- `npm run build` — production build succeeded (pre-existing bundle-size budget warning only, not introduced by this sprint).
- Manually verified: `GET /inventory/stock/movements` (path fix), product activate/deactivate round trip (204/204), `GET /accounting/bills?status=POSTED` (status filter fix).

## Wave 2 — Security & Access Management module

### Routes
- New `/admin/*` mount (users, roles, lookups, screens, user-access) replacing `/accountants/*` as the primary path; old `/accountants/*` paths now redirect client-side (`app-routing.module.ts`). Internal module/folder name (`AccountantsModule`) left unchanged — only the URL and menu seed moved, to keep the diff scoped.
- `/admin/profile` redirects to the existing `/settings` page (profile tab), which now carries a working change-password card — avoided building a duplicate profile UI.
- New `/admin/user-access` page: pick a user, see their assigned custom roles (or "falls back to primary role" hint), and an effective-permissions inspector table (per-screen View/Create/Edit/Delete), backed by a new backend endpoint.
- New `/force-password-change` route + `MustChangePasswordGuard` (`canActivateChild` on the shell route) redirects any authenticated user with `mustChangePassword=true` to a dedicated change-password page before they can reach anything else.

### Backend (Flyway V13)
- `users.must_change_password` (boolean, default false) — settable by an admin on create/edit (`AdminUserFormDto.mustChangePassword`), returned on login and `/profile/me` (`AuthUserDto.mustChangePassword`), cleared automatically on a successful password change.
- `screen_settings` table (screen_key PK, enabled, updated_by/at) + `AdminScreenSettingsController` (`GET/PUT /admin/screen-settings[/{key}]`, ADMIN-only, upsert semantics). Deliberately separate from the existing `ui_menu_items` per-role catalog editor (same "screens" tab, two clearly labeled sections).
- `GET /admin/access/users/{userId}/effective-permissions` — new read endpoint reusing `AccessControlService`'s existing permission-merge logic (`menuPermissionsForUser`) against an arbitrary target user instead of the caller.
- `PUT /profile/me/password` — self-service password change (validates current password via `PasswordEncoder.matches`, clears `mustChangePassword` on success).
- Admin/security menu items (`admin-users`, `admin-roles`, `admin-lookups`, `admin-screens`) had their `url` updated to `/admin/*`; new `admin-user-access` menu item seeded under the same `admin-group`, ADMIN-gated like the rest.

### Frontend
- `AuthService.changePassword()`, `AuthUser.mustChangePassword`.
- `ProfileSettingsComponent` security card: replaced the dead "go re-login to reset your password" link with a real current/new/confirm password form wired to the new endpoint (this UI copy — "Change password" / hint text — already existed unwired in the codebase; only the form + wiring were added).
- `AdminApiService`: `getEffectivePermissions`, `getScreenSettings`, `updateScreenSetting`; `AdminUser`/`AdminUserForm` carry `mustChangePassword`.
- Users tab (accountants-home) gained a "Require password change on next login" field on the create/edit dialog, defaulted on for new users.
- Screens tab gained a "Screen visibility" section (global on/off toggles + add-new-key) above the existing per-role menu catalog editor, clearly separated and labeled.
- `PermissionGuard` wired onto every business module route (`accounts`, `journal-entries`, `general-ledger`, `transactions`, `invoices`, `checks`, `ledger`, `reports`, `inventory`, `sales`, `purchases`, `hr`, `crm`, `manufacturing`, `projects`, `bank-accounts`, `bills`, `budget`, `exchange-rates`, `reconciliation`, `transfers`, `erp/activity-log`) using the real live `ui_menu_items` ids (queried directly from the running DB rather than guessed from migration history). Multi-screen modules (inventory, sales, purchases, hr, crm, manufacturing, projects) are gated on one representative leaf item per module — a per-module rather than per-screen granularity trade-off, since the route mounts a whole lazy module, not a single screen. `/settings` and `/notifications` deliberately left ungated (personal pages every authenticated user needs).
- **Known gap, not closed this wave**: button-level permission gates (`*appHasPermission` on create/edit/delete/export/approve actions) exist as infrastructure and are used on exactly one button (checks create) from before this sprint. Rolling that out across every business page's action buttons is a large, mechanical pass better done as its own wave/PR rather than folded silently into this one.

### Wave 2 verification
- Backend rebuilt and restarted: Flyway migrated schema `erp_system` from v12 → v13 successfully, app started cleanly on port 8087.
- `npm run test:api` — 57/57 passed (no regressions).
- `npm run build` — production build succeeded (same pre-existing bundle-size budget warning, unrelated to this wave).
- Manually verified new endpoints end-to-end: login/`/profile/me` now carry `mustChangePassword`; `GET/PUT /admin/access/users/{id}/effective-permissions`; `GET/PUT /admin/screen-settings[/{key}]`; `PUT /profile/me/password` correctly rejects a wrong current password (400).

## Wave 3 — Notification producers

### Critical bug found and fixed
`NotificationService.currentUserId()` checked `principal instanceof AppUserPrincipal`, but the JWT filter chain's actual runtime principal type is `JwtPrincipal` (confirmed by the same pattern in `AccessControlService`/`UserProfileService`). `AppUserPrincipal` is only used pre-JWT, inside `CustomUserDetailsService` for the login `DaoAuthenticationProvider`. Net effect: **every** `/notifications/*` self-service endpoint (`GET /my`, `GET /my/unread-count`, `PATCH /{id}/read`, `PATCH /my/read-all`) has always returned 400 `AUTH.ERRORS.INVALID_REQUEST` for real authenticated requests — the notification inbox has never worked, independent of whether anything produces notifications. Fixed the `instanceof` check; verified all four endpoints now function (unread-count, list, mark-read, mark-all-read all return 200 and behave correctly).

### Producers wired (backend)
- `NotificationService.notifyAdmins(...)` — new convenience method, resolves all active ADMIN users as recipients (added `UserRepository.findByRoleAndActiveTrue`).
- `JournalEntryService.approveJournalEntry()` — notifies on approve/post (the two share one code path).
- `LeaveRequestService.create()` — notifies on submission (status → PENDING).
- `LeaveRequestService.approve()` — notifies on approval.
- `StockService.approveMovement()` — notifies once when a movement drops a product's total quantity **across the reorder-level threshold** (before ≥ reorder, after < reorder), not on every movement while already low, to avoid spam.
- Sales/purchase document approval and OTP-sent notifications were listed as optional in the task spec and were not wired this wave — flagging as deferred rather than silently skipping.

### Frontend
- `NotificationsPageComponent.onClick()` now deep-links via a `referenceType → route` map (`JOURNAL_ENTRY → /journal-entries`, `PRODUCT → /inventory/products`, `LEAVE_REQUEST → /hr/leave-requests`) in addition to marking the item read. Note: this app's list pages are dialog-driven (no `/entity/:id` detail routes), so "deep-link to detail" resolves to the owning list page, not a specific record — a structural constraint of the existing routing, not something this wave could close without a larger routing change.
- 14-day recent/older tabs and mark-all-read were already implemented; unchanged.

### Wave 3 verification
- Backend rebuilt clean, restarted on port 8087, schema unchanged (no new migration needed).
- `npm run test:api` — 57/57 passed both before and after the notification bugfix.
- Manually verified end-to-end: created + approved a journal entry via the live API → unread count 0→1 → fetched the notification and confirmed `titleKey`/`bodyKey`/`varsJson`/`referenceType`/`referenceId` all correct → mark-read → mark-all-read, all 200.

## Wave 7 — Smoke tests & docs

Extended `erp-system-frontend/scripts/api-integration-test.mjs` from 57 to 94 checks, adding coverage explicitly called for in the task spec:
- All `/admin/access/*` endpoints (context, users, roles, effective-permissions) + a full role create/update/delete round-trip
- `/admin/ui/menu-items` and `/admin/screen-settings` (new in Wave 2) read + toggle round-trip
- Lookups admin CRUD round-trip (type + value, create → read → update → delete)
- Notifications inbox: list, unread-count, mark-all-read
- Reconciliation: list, bank-accounts, create, summary
- Ledger (`GET /accounting/ledger?accountId=`) and both accounting reports (`profit-loss`, `balance-sheet`)
- Password reset OTP send (dev log mode, no mail server needed)
- One POST workflow per domain: journal entry create+approve, stock movement create+submit

### Two real bugs found and fixed by the new coverage
Both are the same root cause in `AdminAccessManagementService`: a delete-then-re-insert pattern (`repository.deleteByRoleId(...)` / `deleteByUserId(...)` followed by a loop of `.save(...)`) relies on the delete hitting the database before the inserts — but Hibernate's default flush order runs **insertions before deletions** within a single flush, regardless of call order. So re-saving the *same* menu-item permission on a role edit, or re-assigning the *same* custom role(s) to a user on a user edit, threw a duplicate-key 500 on the `(role_id, menu_item_id)` / `(user_id, role_id)` unique constraints. Both were previously unexercised by any test.
- `syncPermissions()` (role edit) — reproduced by the new `PUT /admin/access/roles/{id}` test (kept the same permission set on update); fixed with an explicit `roleMenuPermissionRepository.flush()` between the delete and the inserts.
- `syncAssignments()` (user edit) — same pattern, not caught by the automated suite (would need a second user-CRUD round-trip test) but found by manually reasoning about the identical code shape and confirmed via a manual repro: create a user with a custom role, then edit that same user keeping the same `roleIds` → 500 before the fix, 200 after. Fixed the same way.

### Verification
- `npm run test:api` — 94/94 passing after both fixes (was 93/94 with the role-permission bug surfaced, before that this coverage didn't exist).
- Manually reproduced and reverified the user-role-reassignment fix separately (create → edit-with-same-roles → 200, then deactivated the test user since there's no delete-user endpoint by design).
- Backend rebuilt and restarted cleanly on port 8087 for every verification pass in this wave.

## Wave 6 — Routing & shell hardening

- **Root `/`**: now redirects to `/dashboard` instead of unconditionally to `/auth/signin`. `AuthGuard` on the dashboard route still bounces unauthenticated users to sign-in, so the net behavior is: authenticated → dashboard, unauthenticated → sign-in, in one hop either way.
- **AuthGuard race condition, investigated and ruled out**: unlike the Wave 1 `AdminGuard`/`currentUser$` bug (which depended on an async `/profile/me` fetch), `AuthGuard` reads `isAuthenticated$`, a `BehaviorSubject` seeded *synchronously* from `localStorage` token presence at construction — no network round-trip involved, so there's no equivalent race to fix. Confirmed by reading `AuthService` rather than assumed.
- **404 page**: new `NotFoundComponent` (wraps the existing `EmptyStateComponent`, adds a "back to dashboard" action) wired as the last child route under the authenticated shell (`{ path: '**', component: NotFoundComponent }`, after all real routes). `AuthGuard` on the parent still applies, so unauthenticated users hitting a bad URL still land on sign-in; authenticated users now see a real 404 inside the app shell instead of being silently bounced to sign-in.
- **Sidebar fallback menu**: `NavigationService.get()` already fell back to the last-cached menu on `/ui/menu` failure; it now falls back further to a minimal hardcoded safe set (Dashboard, Settings, System Management) when there's *no* cache either — first login on a fresh browser, cleared storage, or right after a cache-clearing deploy no longer means a silently empty sidebar.
- **Command palette**: was missing 11 of the ~24 real business/admin routes entirely (inventory, sales, purchases, hr, crm, manufacturing, projects, bills, budget, exchange-rates, transfers, and every `/admin/*` sub-screen — it still pointed at the old `/accountants` alias). Added all of them, grouped into new "Modules" and "System Management" sections.
- **i18n hardcodes fixed**: dashboard's low-stock badge had a hardcoded Arabic ternary (`'مرتفع' : 'طبيعي'`) that never changed with the language switch — now uses `DASHBOARD.STOCK_HIGH`/`STOCK_NORMAL`. The three `exportAoAToStyledExcel` call sites (generic data-table export, profit & loss export, balance sheet export) had hardcoded English sheet names regardless of UI language — now translated (`COMMON.EXPORT_SHEET_NAME`, `REPORTS.PROFIT_LOSS.TITLE`, `REPORTS.BALANCE_SHEET.TITLE`). File names left as-is (ASCII, already overridable per call site) — lower risk/value than the sheet names, which is what a user actually sees when opening the file.
- **`index.html` lang bootstrap**: confirmed hardcoded `lang="ar" dir="rtl"` on the root `<html>` tag, with no build-time or index.html-level logic reacting to language choice — Angular presumably patches `document.documentElement` post-bootstrap. Left as-is this wave: fixing the pre-Angular-paint flash-of-wrong-direction properly needs the language-detection logic duplicated into a tiny inline bootstrap script (there's already one in `index.html` for the auth-page body-lock classes), which is more surface area than the rest of this wave and deserves its own focused pass rather than a rushed edit to a file that runs before any framework safety net exists.

### Verification
- `npm run build` — clean, same pre-existing bundle-size budget warning only.
- `npm run test:api` — 94/94 passed (no backend changes this wave, so no rebuild needed).

## Wave 4 — Audit trail rollout

`app-erp-audit-trail` was only wired on 3 screens (accountants users tab, general-ledger detail, HR employees) despite being built and ready. Rolled it out to the 5 accounting document screens with edit dialogs: bills, invoices, checks, transactions, budget.

- Found `CustomerInvoiceDisplayDto` (backend) had no `createdAt`/`updatedAt` at all, unlike its siblings (`BillDisplayDto`, `BudgetDisplayDto`, `AccountingCheckDisplayDto`, `AccountingTransactionDisplayDto` all already exposed them) — added the two fields + mapped them in `CustomerInvoiceService.toDisplay()`, matching the existing sibling pattern exactly (entity already extended `BaseEntity`, so the data already existed, just wasn't surfaced).
- Added `createdAt`/`updatedAt` to the 5 corresponding frontend DTOs (they also weren't declared on the TS interfaces even though the backend already sent them for 4 of the 5).
- Each of the 5 page components now tracks the currently-open record (`editingRecord`) set in `patchForm()`, and the edit/view dialog renders `<app-erp-audit-trail [createdAt] [updatedAt]>` gated on `formMode !== 'create'`.
- Completed the rollout across all top-level CRUD/edit-dialog screens: the 20 remaining `ErpMasterPageBase` screens (banks, exchange rates, inventory masters/movements, customers/suppliers/payments, HR, CRM, and projects), the 7 shared-shell sales/purchase document screens, plus accounts, journal entries, payment/receipt vouchers, work orders, and BOM lines.
- Centralized selected-record tracking in `ErpMasterPageBase` and `ErpDocumentPageBase`, so future screens built on either base inherit a consistent audit-record lifecycle (cleared on create/close, populated from the detail GET on edit/view).
- Extended `AccountDisplayDto`/`AccountDto` with `createdAt`/`updatedAt`; the remaining ERP DTOs already exposed timestamps. `ErpAuditTrailComponent` now also accepts the backend's `updatedBy` naming alongside its existing `modifiedBy` input.
- Nested project task/member/expense rows are also covered: their existing backend DTOs already expose `createdAt`/`updatedAt`, and the detail panel now renders the shared audit component for each sub-record. Wave 4 therefore has no remaining screen exception.

## Wave 5.3 — Company Settings module

Full-stack, scoped exactly as specified: company legal name (EN/AR), tax ID, logo, fiscal year start month — kept separate from accounting posting settings.

- **Backend**: reused the existing generic `accounting_settings` key-value table (no new migration needed) under a distinct `company.*` key namespace, with its own `CompanySettingsService`/`CompanySettingsController` (`GET/PUT /settings/company`) rather than folding into `AccountingSettingsService`. GET is available to any authenticated user (company name may be shown in headers/reports); PUT is `@PreAuthorize("hasRole('ADMIN')")`.
- **Frontend**: new `CompanySettingsComponent` (logo upload via the same base64/FileReader pattern as profile settings) added as a third tab on the existing `/settings` page, plus a dedicated `/settings/company` route (tab pre-selected via route data, same pattern as the accountants module). Edit controls disabled client-side for non-admins (real enforcement is server-side).
- New `MONTH.JANUARY..DECEMBER` i18n keys added (didn't exist anywhere in the codebase before this).

### Wave 4 + 5.3 verification
- Backend package rebuilt clean with Java 17 (`mvnw -DskipTests package`; no schema change).
- Full backend unit suite now passes without skipping tests: `mvnw clean test` — 29/29 passed. Updated `JournalEntryServiceTest` to mock the notification producer added in Wave 3 and verify the approval notification payload; `AccountingReportServiceTest` also completes normally (4/4), so the earlier hang warning is closed.
- `npm run test:api` — 94/94 passed.
- `npm run build` — production build passed; the pre-existing initial-bundle budget warning remains (3.57 MB vs 3.50 MB).
- Manually verified: `GET/PUT /settings/company` round-trip (wrote a real company name/tax ID/fiscal month, read it back correctly); confirmed `GET /accounting/invoices` now returns `createdAt` on every row.

## Wave 5 (remainder), 6
Fixed Assets (register, categories, depreciation schedules, disposal, GL integration) and Tax/VAT (tax codes, rates, groups, applied across sales/purchase documents) were not attempted this session — both require new schema, multiple CRUD screens, and integration into existing document DTOs/posting logic, making them substantial standalone efforts on the order of what Waves 1-3 took combined. A Security Dashboard (session/login KPIs) was also not built. Wave 4's rollout is now complete, including nested project sub-records.
