# Switch to main after a PR has been merged

Checks out main, pulls the latest changes, and deletes the feature branch.

If $ARGUMENTS contains `--test`, run in test mode: show what commands would be executed without running them.

1. Run `git branch --show-current` to get the current branch name. Save it.
2. Run `gh pr view "$branch" --json title,url -q '[.title, .url] | @tsv'` to get the PR title and URL. Save both.
3. If in test mode: display the commands that would be run without executing them, then stop.
4. Run `git checkout main`
5. Run `git fetch && git pull`
6. Run `git branch -d <saved-branch>` to delete the old branch (use -d, not -D, since it should already be merged)
7. Report what was done: which branch was deleted, the PR title and URL, and that main is now up to date.
