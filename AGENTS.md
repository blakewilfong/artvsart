# Repository Guidance

- Read `docs/PRODUCT_VISION.md` before planning product changes.
- Inspect `git status` and `git diff` before editing.
- Preserve all existing user changes, including unrelated and uncommitted work.
- Prefer clean object-oriented boundaries. Do not duplicate domain rules in controllers or templates.
- Keep question generation extensible through the existing question strategy architecture.
- Put game calculations in domain or service classes and cover them with unit tests.
- Use PowerShell-compatible commands because development occurs on Windows.
- Run focused tests during implementation, then run `.\mvnw test` before declaring work complete.
- Do not commit or push unless the user explicitly asks.
- Only push working, tested builds.
- Prefer focused edits over wholesale rewrites.
- Explain meaningful design tradeoffs before introducing new abstractions or dependencies.
- Avoid feature creep. Finish the current playable loop before expanding scope.
