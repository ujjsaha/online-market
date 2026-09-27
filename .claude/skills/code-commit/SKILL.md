---
name: code-commit
description: Commit the current changes on a feature branch, push it, and open a GitHub pull request. Use when the user asks to commit and raise/open/create a PR.
argument-hint: "[optional notes, e.g. 'fixes #42' or 'base: develop']"
disable-model-invocation: true
allowed-tools: Bash(git status *) Bash(git diff *) Bash(git log *) Bash(git branch *) Bash(git switch *) Bash(git checkout -b *) Bash(git add *) Bash(git commit *) Bash(git push *) Bash(git remote *) Bash(git rev-parse *) Bash(gh pr create *) Bash(gh pr view *) Bash(gh auth status *)
---

## Repository state

- Current branch: !`git branch --show-current`
- Default branch: !`git symbolic-ref --short refs/remotes/origin/HEAD 2>/dev/null | sed 's@^origin/@@' || echo main`
- Status:
!`git status --short`
- Diff summary:
!`git diff HEAD --stat`
- Recent commits (for message style):
!`git log --oneline -8`

## Full diff

```!
git diff HEAD
```

## User notes

$ARGUMENTS

## Instructions

Follow these steps in order. Stop and ask the user if anything is ambiguous or risky.

1. **Check there is something to commit.** If the status above is empty, say there are no changes and stop.

2. **Review the diff before committing.** Look for:
   - Secrets, tokens, API keys, `.env` files or credentials — if found, stop and warn the user. Never commit them.
   - Debug leftovers (`console.log`, `print`, `debugger`, commented-out blocks), unrelated files, large binaries.
   Mention anything suspicious and ask whether to include it.

3. **Get onto a feature branch.** If the current branch is the default branch (or `main`/`master`/`develop`), create a new branch with `git switch -c <type>/<short-kebab-description>` (e.g. `feat/add-login-rate-limit`, `fix/null-user-crash`). Never commit directly to the default branch.

4. **Stage the changes.** Stage the relevant files by name with `git add <files>`. Avoid `git add -A` unless every changed file clearly belongs in this commit.

5. **Write the commit message** using Conventional Commits, matching the style of recent commits if the repo uses a different convention:
   ```
   <type>(<optional scope>): <imperative summary, max 72 chars>

   <body: what changed and why, wrapped at 72 chars>

   <footer: e.g. Closes #42>
   ```
   Types: feat, fix, refactor, perf, test, docs, chore, build, ci, style.
   If the changes are unrelated to each other, make separate commits.
   Commit with `git commit -m "<summary>" -m "<body>"`.

6. **Push** with `git push -u origin <branch>`. If the push is rejected, show the error and stop — do not force-push.

7. **Open the pull request** against the default branch (or the base the user named in their notes):
   - Preferred: `gh pr create --base <base> --title "<title>" --body "<body>"`. Run `gh auth status` first; if `gh` is missing or not logged in, use the GitHub MCP server's create pull request tool instead.
   - Title: same as the commit summary (or a summary of all commits).
   - Body:
     ```
     ## Summary
     <1–3 sentences on what and why>

     ## Changes
     - <bullet per meaningful change>

     ## Testing
     - <how it was tested, or "Not tested — please verify">

     <Closes #N if the user mentioned an issue>
     ```
   - Open as a draft (`--draft`) if the user says the work is incomplete.

8. **Report back** with the branch name, commit hash(es), and the PR URL. Keep it short.