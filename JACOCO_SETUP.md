# JaCoCo Code Coverage Setup

## Overview
JaCoCo (Java Code Coverage) has been configured in the Maven project to automatically generate code coverage reports during test execution. This setup requires **no production code changes**.

---

## Configuration Details

### POM.xml Changes
- **Plugin:** `org.jacoco:jacoco-maven-plugin` version `0.8.12`
- **Location:** `pom.xml` under `<build><plugins>`
- **Goals:**
  - `prepare-agent` - Runs before test execution to instrument code
  - `report` - Runs during test phase to generate HTML report

### Report Output
- **Location:** `target/site/jacoco/`
- **Format:** HTML (interactive coverage report)
- **Main File:** `index.html`

---

## How to Generate Coverage Report

### Option 1: Run All Tests with Coverage
```bash
mvn clean test
```
This automatically:
1. Compiles production code
2. Instruments classes with JaCoCo agent
3. Runs all JUnit 5 tests
4. Generates HTML coverage report in `target/site/jacoco/`

### Option 2: Run Specific Tests with Coverage
```bash
mvn clean test -Dtest=org.example.service.AdminAuthServiceTest
```

### Option 3: Skip Tests but Generate Report from Previous Run
```bash
mvn jacoco:report
```

---

## Viewing the Coverage Report

### Interactive Report
1. Open a file browser and navigate to: `target/site/jacoco/`
2. Double-click `index.html` to open in web browser
3. Explore coverage by package/class/method

### Report Structure
- **Overall coverage** at the top
- **Package-level breakdown** showing line/branch/instruction coverage
- **Class-level details** with highlighted covered/uncovered lines
- **Method-level metrics** showing complexity and coverage

---

## Coverage Metrics Explained

- **Instructions:** Low-level bytecode coverage (most granular)
- **Branches:** If/else, switch statement coverage
- **Lines:** Source line coverage (practical metric)
- **Methods:** Method execution coverage
- **Cyclomatic Complexity:** Code path complexity counter

---

## Sample Maven Output

When running `mvn clean test`, you'll see:
```
[INFO] Building appointment-system 1.0-SNAPSHOT
[INFO] ...
[INFO] --- maven-surefire-plugin:3.2.5:test (default-test) @ appointment-system ---
[INFO] Tests run: 52, Failures: 0, Skipped: 0, Time: 2.345s
[INFO] ...
[INFO] --- jacoco-maven-plugin:0.8.12:report (report) @ appointment-system ---
[INFO] Analyzing /path/to/target/classes ...
[INFO] Report generated at /path/to/target/site/jacoco/index.html
[INFO] BUILD SUCCESS
```

---

## Existing Test Compatibility

✅ **JUnit 5 Integration**
- Works seamlessly with `junit-jupiter` (already in pom.xml)
- No test modifications needed
- Mockito integration unaffected

✅ **Surefire Plugin**
- maven-surefire-plugin (3.2.5) runs all tests
- JaCoCo agent enabled automatically
- Test output reports as usual

✅ **No Production Code Changes**
- Zero impact on application logic
- No new dependencies for production
- Build artifacts unchanged

---

## Troubleshooting

### Report Not Generated
**Problem:** No `target/site/jacoco/index.html` after `mvn test`

**Solution:**
```bash
mvn clean test
# Verify no test failures
# Check target/site/jacoco/ directory exists
```

### Coverage Shows 0%
**Problem:** Report shows 0% coverage despite tests passing

**Solution:** This should not occur with current setup. If it does:
1. Verify tests actually ran (check test count in Maven output)
2. Run `mvn clean` before test to clear cached bytecode
3. Check that test files are in `src/test/java/`

### Old Report Still Showing
**Problem:** Changes to test code don't reflect in report

**Solution:**
```bash
mvn clean test  # Always use 'clean' to remove old reports
```

---

## Best Practices

1. **Always use `mvn clean test`** - Removes stale reports
2. **Check report regularly** - Maintain code coverage above 80%
3. **Use coverage metrics** - Identify untested code paths
4. **Write tests for critical code** - Especially service/repository layers
5. **CI/CD Integration** - Add coverage threshold checks in pipeline (future)

---

## Files Modified

- **pom.xml** - Added/enhanced JaCoCo configuration
- **No production code modified**
- **No test code modified**

---

## Next Steps

- Run `mvn clean test` to generate your first coverage report
- Open `target/site/jacoco/index.html` in a browser
- Identify untested code paths
- Write additional tests to improve coverage


