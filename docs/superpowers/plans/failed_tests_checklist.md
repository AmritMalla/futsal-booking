# Failed Tests / Deferred Issues Checklist

Tracks test failures and unresolved issues from the EKS deployment plan that we deliberately moved past during execution. To be addressed after all plan tasks are complete.

## Failures

### 1. `KubernetesProfileTest#prometheusEndpointIsExposed`

- **File:** `src/test/java/com/amrit/futsal/config/KubernetesProfileTest.java`
- **Task:** 1.2
- **Commits in tree:** `cf496b4` (initial), `b424901` (corrected to use `/actuator/prometheus`)
- **Symptom:** Test asserts `GET /actuator/prometheus` returns 200, but it returns 500 with `org.springframework.web.servlet.NoHandlerFoundException: No endpoint GET /actuator/prometheus`.
- **Likely cause:** `micrometer-registry-prometheus` (added in `f1f9259`) isn't being resolved into the test classpath, OR `application.properties` has `spring.mvc.throw-exception-if-no-handler-found=true` combined with the exception flowing through `GlobalExceptionHandler`. Need to:
  1. Confirm `micrometer-registry-prometheus` shows in `./mvnw dependency:tree -Dincludes=io.micrometer:micrometer-registry-prometheus`.
  2. Check whether `PrometheusMeterRegistryAutoConfiguration` is loaded under `kubernetes` profile.
  3. Verify `management.endpoints.web.exposure.include=health,info,prometheus` is winning over any base-properties merge.
  4. If still missing, suspect a Spring Boot dependency convergence issue or that the actuator's prometheus endpoint registration races with security filter setup.
- **Other 2 tests in same class:** `livenessEndpointIsExposed` and `readinessEndpointIsExposed` PASS.

### 2. Frontend Docker build — recharts Tooltip Formatter type mismatch

- **File:** somewhere under `frontend/src/` that uses `recharts` `<Tooltip formatter=...>`. Likely `frontend/src/pages/admin/AdminDashboard.tsx` or `frontend/src/pages/owner/Dashboard.tsx` (analytics charts).
- **Task:** 2.1 (frontend Dockerfile)
- **Symptom:** `npm run build` inside `node:20-alpine` fails with TypeScript error:
  `TS2322: Type '(value: number | undefined) => string' is not assignable to type 'Formatter<ValueType, NameType> & ((value: ValueType, name: NameType, item: TooltipPayloadEntry, index: number, payload: TooltipPayload) => ReactNode | [...])'.`
- **Likely cause:** `recharts` was upgraded from the original lock to 3.7.0 when `package-lock.json` was regenerated to fix the `npm ci` "out of sync" error. The 3.x typings tightened the `Formatter` signature so the existing `(value) => string` lambdas no longer satisfy it.
- **Fix options:**
  1. Pin `recharts` back to a 2.x release in `frontend/package.json` (matches the original API surface).
  2. Update each affected `formatter` to take `(value, name, item, index, payload)` — only need to use `value`.
  3. Cast: `formatter={((value: number | undefined) => fmt(value)) as any}`.
- **Local build status:** Local `npm run build` PASSED before lock regen with the previous `recharts` version. Failure only appears in Docker build with regenerated lock.
- **Image build verification deferred:** Backend image (`futsal-backend:local`) builds successfully. Frontend image build is blocked until this is resolved.

## Resolved (kept here as audit trail)

_(none yet)_
