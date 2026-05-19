# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 2/2 (100.0%)
- **Function parity:** 40/51 matched (target 64) — 78.4%
- **Class/type parity:** 12/13 matched (target 26) — 92.3%
- **Combined symbol parity:** 52/64 matched (target 90) — 81.2%
- **Average inline-code cosine:** 0.54 (function body across 2 matched files)
- **Average documentation cosine:** 0.40 (doc text across 2 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 1 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. lib

- **Target:** `mime.Mime`
- **Similarity:** 0.47
- **Dependents:** 0
- **Priority Score:** 84805.3
- **Functions:** 29/37 matched (target 52)
- **Missing functions:** `fmt`, `has_params`, `eq`, `partial_cmp`, `cmp`, `hash`, `from`, `size_hint`
- **Types:** 11/11 matched (target 20)
- **Missing types:** _none_
- **Tests:** 11/12 matched

### 2. parse

- **Target:** `mime.Parse`
- **Similarity:** 0.61
- **Dependents:** 0
- **Priority Score:** 41603.9
- **Functions:** 11/14 matched (target 12)
- **Missing functions:** `fmt`, `new`, `next`
- **Types:** 1/2 matched (target 6)
- **Missing types:** `Item`
- **Tests:** 3/3 matched

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

## Next Commands

```bash
# Initialize task queue for systematic porting
cd tools/ast_distance
./ast_distance --init-tasks ../../src rust ../../src/commonMain/kotlin/io/github/kotlinmania/mime kotlin tasks.json ../../AGENTS.md

# Get next high-priority task
./ast_distance --assign tasks.json <agent-id>
```
