# Review a pull request

Checks out a PR branch locally, runs a full code quality and spec compliance review, then returns to the original branch.

## Input

$ARGUMENTS should be in the form `<repo-name>#<pr-number>` (e.g. `rimfrost-handlaggning-regel#42`) or a full GitHub PR URL, optionally followed by `--model <model-name>`.

If no repo is specified and a PR number is given alone, assume the current directory's repo.

The `--model <model-name>` flag delegates the entire code quality review to a sub-agent running the specified model (e.g. `--model opus`, `--model sonnet`). Without this flag, the review runs inline with the current model.

## Examples

```
/pr-review 42
/pr-review rimfrost-handlaggning-regel#42
/pr-review https://github.com/Forsakringskassan/rimfrost-handlaggning-regel/pull/42
/pr-review 42 --model opus
/pr-review 42 --model sonnet
```

## Steps

1. Parse $ARGUMENTS to extract the repo name and PR number.

2. Resolve the repo path:
   - For FK/Rimfrost projects: `/Users/ulf/fk/github/<repo-name>`
   - For a plain PR number with no repo: use the current working directory

3. Run `git -C <repo-path> status --porcelain`. If there are any uncommitted changes, abort immediately with a message listing the dirty files and instructing the user to commit or stash them before retrying.

4. Record the current branch: `git -C <repo-path> branch --show-current`

5. Check out the PR branch: `gh pr checkout <pr-number> --repo <owner>/<repo-name>` from within the repo path.

6. Run a full review following the rules in `~/.claude/commands/_reviewer-base.md` and any project-level reviewer role file found at `.claude/commands/_reviewer-role.md` in the repo.
   - Also check the repo's CLAUDE.md for a `Spec reviewer agent:` entry under `## Review`. If found, spawn that agent using the Agent tool in parallel with your own code quality review. Combine both outputs — code quality first, then spec compliance.
   - If `--model <model-name>` was passed, delegate the entire code quality review to a sub-agent via the Agent tool using the specified model. Pass the full reviewer rules and any project-level role file content to the sub-agent prompt. Run the spec reviewer agent (if applicable) in parallel.

7. After the review is complete, return to the original branch: `git -C <repo-path> checkout <original-branch>`

8. Remind the user which branch they are back on.
