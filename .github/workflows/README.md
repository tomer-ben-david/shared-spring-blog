# Shared GitHub Workflows

This repository centralizes shared GitHub Actions workflows. Currently it contains the Discord and Slack notifications workflow that listens to pushes, issues, PRs, comments, reviews, and a manual trigger.

## Usage
- Secrets: by default the workflow reads `DISCORD_WEBHOOK_URL` and `SLACK_WEBHOOK_URL`. To use different secret names, set repo variables:
  - `DISCORD_WEBHOOK_SECRET_NAME` - custom name for Discord webhook secret
  - `SLACK_WEBHOOK_SECRET_NAME` - custom name for Slack webhook secret
- Both webhooks are optional - the workflow will send to whichever ones are configured.
- Triggers: push to `main`/`master`, issue/PR/review events, and `workflow_dispatch` (with optional `commit_sha`).
- Location: subtree the repo directly into `.github/workflows` so GitHub discovers the workflow at `.github/workflows/discord-notifications.yml`.

## Dogfooding
- This repo now runs the same workflow from `.github/workflows/discord-notifications.yml` so pushes/issues/PRs here notify Discord and Slack too.
- Keep `discord-notifications.yml` and `.github/workflows/discord-notifications.yml` identical; run `cp discord-notifications.yml .github/workflows/discord-notifications.yml` after edits.
- Add the repo secrets `DISCORD_WEBHOOK_URL` and/or `SLACK_WEBHOOK_URL` (or whatever custom names you configured) with your webhook URLs so notifications fire.

## Subtree commands
Replace `<remote>` with the remote/URL for this repo (local path or GitHub remote) and run from a consumer repo.

```sh
git remote add devx-gh-actions <remote>
# First add
git subtree add --prefix .github/workflows devx-gh-actions main --squash
# To pull updates later
git subtree pull --prefix .github/workflows devx-gh-actions main --squash
```
