---
name: create-feat-branch
description: Create a new feat/NAME branch from the latest develop branch of a GitHub repo and push it. Use when the user runs /create-feat-branch with a name or asks to start a feature branch off develop.
disable-model-invocation: true
---

# Create feature branch from develop

Creates `feat/<name>` from an up-to-date `develop` and pushes it to `origin` with upstream tracking.

## Input

- The branch name comes from the slash command argument, e.g. `/create-feat-branch user-login`.
- If no argument was given, ask the user for the feature branch name before doing anything else.

## Normalize the name

1. Trim whitespace, lowercase it, replace spaces and underscores with `-`, and drop characters other than `a-z 0-9 - / .`.
2. If the name already starts with `feat/`, keep it as is; otherwise prefix it with `feat/`.
3. Validate with `git check-ref-format --branch "<branch>"`. If it fails, tell the user why and ask for another name.
4. Tell the user the final branch name you will create.

## Steps

Run these in order and stop on the first failure, reporting the error plainly.

1. **Confirm it's a git repo with a GitHub remote**
   - `git rev-parse --is-inside-work-tree`
   - `git remote get-url origin` — if there's no `origin`, stop and tell the user.

2. **Check for uncommitted changes**
   - `git status --porcelain`
   - If there are changes, stop and ask the user whether to stash them (`git stash push -u -m "auto-stash before <branch>"`), commit them first, or carry them onto the new branch. Do not discard anything.

3. **Fetch latest**
   - `git fetch origin --prune`
   - Confirm `origin/develop` exists: `git show-ref --verify --quiet refs/remotes/origin/develop`. If not, stop and tell the user there is no `develop` branch on origin.

4. **Make sure the branch doesn't already exist**
   - Local: `git show-ref --verify --quiet refs/heads/<branch>`
   - Remote: `git ls-remote --exit-code --heads origin <branch>`
   - If either exists, stop and ask the user whether to check out the existing branch or pick a new name.

5. **Update develop**
   - `git checkout develop` (if it doesn't exist locally: `git checkout -b develop --track origin/develop`)
   - `git pull --ff-only origin develop`
   - If the fast-forward fails (local develop has diverged), stop and tell the user; don't merge or reset on their behalf.

6. **Create the feature branch and switch to it**
   - `git checkout -b <branch>` — this creates the branch from `develop` and makes it the current working branch.
   - Do not switch back to `develop` or the original branch afterwards.

7. **Push and set upstream**
   - `git push -u origin <branch>`

8. **Restore stash if one was made in step 2** (only if the user chose to carry changes over): `git stash pop`.

9. **Confirm the feature branch is the current working branch**
   - `git branch --show-current` must print `<branch>`.
   - If it doesn't, run `git checkout <branch>` and check again. If it still isn't current, stop and tell the user.

## Report back

In two or three lines: the branch name (confirming it's now the current working branch), the develop commit it was created from (`git rev-parse --short HEAD`), and that it's pushed to origin with tracking. If `gh` is installed, include the branch URL from `gh browse -n --branch <branch>`.
