# Suggest a pull request title and description

Inspects the current branch's commits and diff, suggests a PR title and description, and creates the PR on approval.

If $ARGUMENTS contains `--test`, run in test mode: show what commands would be executed and what actions would be taken, but do not execute anything or ask for approval.

1. Run `git branch --show-current` to get the branch name.
2. Run `git log main..HEAD --oneline 2>/dev/null || git log master..HEAD --oneline` to list commits on this branch.
3. Run `git diff main...HEAD 2>/dev/null || git diff master...HEAD` to see all changes.
4. Based on the branch name, commits, and diff, suggest:
   - A concise PR **title** (under 70 characters, imperative mood)
   - A **description** with a brief summary of what changed and why
   - No test plan section
5. If NOT in test mode: ask for approval, then create the PR with `gh pr create`.
   If in test mode: display the `gh pr create` command that would be run with the title and body filled in, without executing it.

Do not add Claude branding anywhere.
