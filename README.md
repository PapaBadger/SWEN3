# GIT Usage Basics

## Start a feature (you currently only have main)

`git checkout main`

`git pull origin main`

### Create and switch to a feature branch

`git checkout -b feature/<short-name>`   # e.g., feature/login

`git push -u origin feature/<short-name>`  # publish branch (once)

## Work on feature

### Add files and commit often (small, focused commits)

`git add -A`

`git commit -m "feat(login): show login form"`

### Push your progress

`git push`

## Keep  your branch fresh if main moved ahead
(Do this before you finish/merge)

`git checkout main`

`git pull origin main`

`git checkout feature/<short-name>`

`git rebase main`       (!! preferred: linear history)

if conflicts: edit files -> `git add` the fixed files -> `git rebase --continue`
#### to abort rebase: git rebase --abort

### After a rebase, force-push the branch (safe here)
`git push --force-with-lease`

## Finish the feature (merge back to main)

### Option A – Fast-forward (clean & simple, after rebase)

`git checkout main`

`git pull origin main`

`git merge --ff-only feature/<short-name>`   # will fail if not a fast-forward

`git push origin main`

### Option B – Squash merge (one commit on main)

`git checkout main`

`git pull origin main`

`git merge --squash feature/<short-name>`

`git commit -m "feat(<scope>): <summary> (#<issue/PR>)"`

`git push origin main` 

## Clean up merged branches

`git branch -d feature/<short-name>`                  # delete local

`git push origin --delete feature/<short-name>`       # delete remote