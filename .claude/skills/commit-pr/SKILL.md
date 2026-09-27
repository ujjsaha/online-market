---
name: commit-pr
description: Commit the current git changes with a Conventional Commits message, push the branch, and open a GitHub pull request with the gh CLI. Use this whenever the user types /commit-pr or asks to "commit and raise a PR", "open a PR for this", "ship these changes", "push and create a pull request", or otherwise wants their working-tree changes turned into a commit plus a PR — even if they only say "commit this and make a PR".
---

# Commit & Pull Request

Turn the user's current working-tree changes into a well-described commit on a feature branch, push it, and open a ready-for-review GitHub PR with `gh`. The goal is a PR a reviewer can understand without asking questions, produced with as little back-and-forth as possible.

Any text the user passes after the command (e.g. `/commit-pr fix login redirect loop, closes #42`) is a hint about intent — use it to shape the commit message, branch name and PR body, and carry over any issue references.

## 1. Check the ground

Run these together and read the results before changing anything:

```bash
git rev-parse --is-inside-work-tree
git status --porcelain=v1 -b
git branch --show-current
git remote -v
gh auth status
```

Stop and tell the user plainly (don't try to work around it) if:
- this isn't a git repo,
- there are no changes at all (nothing staged, modified or untracked) **and** no unpushed commits,
- there's a merge/rebase in progress or unresolved conflicts,
- `gh` isn't installed or authenticated (suggest `gh auth login`),
- there's no GitHub remote.

If there are no changes but there *are* unpushed commits on a feature branch, skip to step 5 — the user most likely just wants the PR.

## 2. Understand the change

Read what actually changed; the message must describe the code, not guess from filenames.

```bash
git diff --staged --stat
git diff --stat
git diff --staged
git diff
git log --oneline -10
```

For untracked files, look at them briefly (names and a skim of content). For very large diffs, read the `--stat` and the most important hunks rather than everything.

**What to include in the commit:**
- If the user already staged something, respect that: commit only what's staged and mention the unstaged leftovers in your final summary.
- If nothing is staged, stage everything relevant with `git add -A`.
- Before staging, scan for files that almost certainly shouldn't be committed — `.env` and other secret/credential files, private keys, large binaries, build output (`dist/`, `node_modules/`, `__pycache__/`), editor/OS junk (`.DS_Store`). Leave those out and tell the user why. Leaking a secret into a public PR is far worse than one extra question.

## 3. Get onto a feature branch

Find the default branch: `gh repo view --json defaultBranchRef -q .defaultBranchRef.name` (fall back to `main`/`master`).

If the current branch is the default branch (or detached HEAD), create a new branch — the uncommitted changes come along automatically:

```bash
git switch -c <type>/<short-kebab-summary>
```

Name it from the change: `<type>/<3–5 word kebab summary>`, e.g. `feat/add-jwt-login`, `fix/login-redirect-loop`. Include an issue number if the user gave one (`fix/42-login-redirect-loop`). If the name exists already, append `-2`.

If already on a feature branch, stay on it.

## 4. Write the commit (Conventional Commits)

Format:

```
<type>(<optional scope>): <imperative summary, lowercase, no period, ≤72 chars>

<body: what changed and why, wrapped at ~72 cols — skip for trivial changes>

<footer: BREAKING CHANGE: ..., Closes #N — only when applicable>
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`. Pick the one that describes the *main* purpose; the scope is the module/area most affected (`auth`, `api`, `ui`). Use `!` after the type/scope plus a `BREAKING CHANGE:` footer when public behavior breaks.

The body is for the *why* — the diff already shows the what. Good: "Tokens were refreshed after expiry, causing a redirect loop on slow networks." Bad: "Changed auth.ts."

If the diff clearly contains unrelated changes (say, a feature plus an unrelated dependency bump), it's fine to make two commits by staging paths separately — but only when the split is obvious; don't agonize over it.

Commit with a heredoc so newlines and quotes survive:

```bash
git commit -F - <<'EOF'
feat(auth): add JWT-based login

Replaces session cookies with short-lived access tokens plus a
refresh token so the mobile client can authenticate.

Closes #42
EOF
```

If a pre-commit hook fails, show the user the error. If the hook auto-fixed files (formatters), re-stage them and commit again; otherwise fix the reported problem if it's clearly mechanical, or stop and ask. Never use `--no-verify` unless the user asks.

## 5. Push

```bash
git push -u origin HEAD
```

If the push is rejected because the remote branch moved, stop and tell the user — don't force-push unless they explicitly ask.

## 6. Open the PR

First check one doesn't already exist: `gh pr view --json url -q .url 2>/dev/null`. If it does, the push already updated it — just report the URL.

Otherwise create it as ready for review against the default branch:

```bash
gh pr create --base <default-branch> --title "<title>" --body-file - <<'EOF'
<body>
EOF
```

- **Title**: the commit subject line (for multiple commits, a summary in the same Conventional style).
- **Body**: if the repo has a PR template (`.github/pull_request_template.md`, `.github/PULL_REQUEST_TEMPLATE.md`, `docs/pull_request_template.md`, or `.github/PULL_REQUEST_TEMPLATE/`), fill that in. Otherwise use:

```markdown
## Summary
<1–3 sentences: what this PR does and why>

## Changes
- <notable change>
- <notable change>

## Testing
<how it was verified, or what the reviewer should check; say honestly if nothing was run>

Closes #N   <!-- only if an issue was referenced -->
```

Open as a draft (`--draft`) only if the user says "draft" or "WIP".

## 7. Report back

Keep it short:
- the branch name and commit subject(s),
- the PR URL,
- anything left out (unstaged files, skipped secrets) and why.

## Things to avoid

- Force-pushing, rewriting history, or committing to the default branch directly — these are hard to undo on a shared repo.
- Inventing test results. If you didn't run tests, the Testing section says so.
- Vague messages like `update files` or `fix stuff`.
