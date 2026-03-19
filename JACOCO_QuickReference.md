# JaCoCo Quick Reference

## One-Line Summary
JaCoCo code coverage has been configured in `pom.xml` to automatically generate HTML coverage reports during test execution.

---

## Single Command to Generate Report
```powershell
mvn clean test
```

---

## Where to Find the Report
```
target/site/jacoco/index.html
```
Double-click to open in your default browser.

---

## What Gets Tested
- All JUnit 5 tests in `src/test/java/`
- Coverage metrics automatically collected during test execution
- HTML report generated after all tests pass/fail

---

## Key Metrics in Report

| Metric | Meaning |
|--------|---------|
| **Line Coverage** | What % of source lines executed during tests |
| **Branch Coverage** | What % of if/else branches executed |
| **Instruction Coverage** | What % of bytecode instructions executed |
| **Cyclomatic Complexity** | Code path complexity (higher = more complex) |

---

## Report Structure
```
index.html
├── Summary (overall coverage %)
├── Packages
│   └── org.example
│       ├── Classes (line coverage %)
│       │   └── Individual methods (line coverage %)
│       └── Source code with color highlights
│           - Green = executed (covered)
│           - Red = not executed (uncovered)
│           - Yellow = partially executed (branches)
```

---

## What Changed
- **pom.xml** - Enhanced JaCoCo configuration only
- **Production code** - No changes
- **Test code** - No changes
- **Build output** - No changes (report is extra)

---

## Troubleshooting Checklist

| Issue | Solution |
|-------|----------|
| Report not generated | Ensure tests passed; check `target/site/jacoco/` directory |
| Old report showing | Always use `mvn clean test` (not just `mvn test`) |
| 0% coverage | This shouldn't happen; verify tests ran (check Maven output) |
| Different report | Delete `target/` folder and re-run `mvn clean test` |

---

## Why This Matters

✅ **Visibility** - See exactly what code is tested  
✅ **Quality** - Identify untested code paths  
✅ **Confidence** - Know your test coverage percentage  
✅ **Improvement** - Track coverage over time  
✅ **CI/CD Ready** - Can enforce minimum coverage requirements  

---

## Files Created/Modified

| File | Type | Purpose |
|------|------|---------|
| `pom.xml` | Modified | Enhanced JaCoCo configuration |
| `JACOCO_SETUP.md` | New | Comprehensive setup guide |
| `JACOCO_Configuration_Complete.md` | New | Detailed configuration doc |
| `JACOCO_QuickReference.md` | New | This file |

---

## Next Action
```powershell
cd C:\Users\inshe\Desktop\Software\appointment-system
mvn clean test
# Then open: target/site/jacoco/index.html
```

Done! 🎉

