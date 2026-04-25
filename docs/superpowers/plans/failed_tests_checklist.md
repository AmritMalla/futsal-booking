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

## Resolved (kept here as audit trail)

_(none yet)_
