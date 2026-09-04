# AGENTS.md

Behavioral guidelines for AI coding agents (Google Antigravity, Claude Code, and other AGENTS.md-compatible tools) to reduce common mistakes. Merge with project-specific instructions as needed.

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

## 1. Read Architecture First (Mandatory)

**Before touching any file, read `ARCHITECTURE.md`, `CODE_STYLE.md`, and `UI_XML_STYLE.md` in full.**

This project enforces strict layer rules, naming conventions, and a canonical directory tree across both Kotlin and XML. Violating them creates technical debt that is expensive to undo.

### Required reading

**`ARCHITECTURE.md`**
- §2 Layer Architecture — dependency direction, what belongs in `data/`, `domain/`, `ui/`.
- §3 Directory Tree — the canonical package skeleton; every new file must land in the correct package.
- §5 Naming Conventions — class suffixes, file names, View ID prefixes, layout names.
- §6 Rules for AI Agents — the placement decision table and the DO / DON'T lists.

**`CODE_STYLE.md`**
- Dependency naming rules (DAO/Repository/Storage — single vs. multiple).
- ViewModel state & event conventions (`StateFlow`, `SharedFlow`/`Channel`, `UiEvent`).
- The `observeViewModels()` lifecycle-observation pattern.

**`UI_XML_STYLE.md`**
- View ID prefixes and root-layout naming.
- The `@id/main` insets requirement for `BaseActivity`.
- Standard button background drawables (§14.6) and when to use each.

If you are unsure where a new class belongs, consult the **"I need to add X, where does it go?"** table in `ARCHITECTURE.md §6` before creating any file.

> For anything beyond these three core docs (project status, reusable templates, agent output rules, Figma extraction, etc.), check the **Read-on-demand map** at the top of each file — don't pull in extra docs you don't need for the task.

### Do not proceed to implementation until you can answer:

1. Which layer does this change live in (`data/`, `domain/`, `ui/`, `di/`, `common/`, `utils/`, `widget/`, ...)?
2. Which existing base class or interface must I extend or implement?
3. Which DI module (`AppModule`, `NetworkModule`, `DatabaseModule`, `DataStoreModule`, `RepositoryModule`, `WorkManagerModule`) must be updated?
4. Which naming convention applies to the new code (dependency naming from `CODE_STYLE.md`, or View ID prefix from `UI_XML_STYLE.md`)?

---

## 2. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:

- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 3. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 4. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:

- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:

- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 5. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:

- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:

```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

## 6. Git Safety

**Never run destructive or automatic git commands.**

Forbidden without explicit user confirmation:

- `git push` — never push to any remote automatically.
- `git commit` — never commit automatically; always show the diff and wait for approval.
- `git reset --hard` / `git clean -fd` — destructive; data loss is permanent.
- `git rebase` / `git merge` / `git cherry-pick` — branch-altering operations; confirm first.
- `git push --force` / `git push --force-with-lease` — never; rewrites remote history.
- `git branch -D` — force-deletes a local branch even if unmerged; data loss is permanent.
- `git stash drop` / `git stash clear` — discards stashed work permanently.

Allowed without confirmation:

- `git status`, `git log`, `git diff`, `git show` — read-only inspection.
- `git add` — staging only (no side effects until `commit`).
- `git branch`, `git checkout -b` — local branch creation is safe.

If the user asks you to "commit and push", **show the staged diff first**, state the proposed commit message, and wait for the user to explicitly say "proceed" before running `git commit`.

---

**These guidelines are working if:** fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.
