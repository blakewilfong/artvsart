# Art vs Art

Art vs Art is an art-based web game built with Java, Spring Boot, Thymeleaf, JPA, and H2 during local development.

The core interaction presents two artworks and asks the player a deterministic factual question. The player chooses an artwork by clicking the image.

## Product principles

- The artwork itself should dominate the interface.
- Keep the interface clean and restrained.
- Do not display artwork titles or artist names before an answer because that can reveal the answer.
- Reveal titles, artists, and relevant factual values after answering.
- Artwork images must never be stretched or cropped.
- Images should use similarly sized frames while preserving their complete aspect ratios.
- Do not show separate Choose buttons. The artwork cards themselves are the controls.
- After displaying the result briefly, automatically advance when the game remains active.
- Questions should be interesting art-history comparisons, not visually trivial prompts such as asking which image depicts a landscape.
- Rules must be deterministic and derived from stored metadata.
- Avoid exposing unnecessary information or clutter during play.

## Current modes

### Streak

- The player continues until answering incorrectly.
- The score is the number of consecutive correct answers.
- A global high score gives the player a record to chase.
- Questions become progressively harder as the run continues.
- Correct answers automatically advance after a short reveal.
- An incorrect answer ends the run and remains on the result screen.

### Wager

- The player starts with 100 chips.
- The player wagers chips based on confidence.
- A correct answer adds profit after rake.
- An incorrect answer loses the entire wager.
- Reaching zero chips ends the run.
- Track a global high score based on the highest chip balance.
- The initial minimum wager is 5 chips.
- The minimum wager compounds by 10% each round and rounds up.
- If the minimum exceeds the current balance, the player must go all-in.
- House rake is 0% during rounds 1 through 5.
- Rake increases by 5 percentage points every five rounds.
- Rake caps at 25%.
- Rake applies only to profit from correct answers.
- Rake increases must be clearly communicated.
- The persistent Wager interface should use a sticky sidebar containing only Chips, House rake, and Minimum bet.
- Chip gains and losses should animate visibly in that sidebar.
- The answer banner should say only Correct or Incorrect without repeating financial details.

### Crowd mode

- Crowd mode is a possible later mode based on predicting which artwork has the higher lifetime community selection rate.
- Some older Matchup, Vote, and ArtworkStatistics code is intentionally retained for it.
- Crowd mode is lower priority than completing and polishing Streak and Wager.

There is no Daily Game mode. It was intentionally removed because it was not compelling enough.

## Implemented factual question types

- Which artwork is older?
- Which artist was born earlier?
- Which artist was younger when they created the artwork?

Question types use the strategy architecture so eligibility, correct-answer selection, and difficulty thresholds remain isolated.

## Candidate future question types

Potentially interesting:

- Which artwork was created before a particular historical event?
- Which artist is from a named country?
- Other metadata-driven comparisons that require genuine art-history judgment.

Rejected or low-value:

- Met highlight status
- Which artist lived longer?
- Visually obvious subject-recognition questions
- Questions whose answers rely on missing or unreliable metadata

Do not add more question types until the current game loops and difficulty progression are stable.

## Difficulty

Difficulty should be tunable through minimum-difference thresholds and similar eligibility rules.

Early rounds should use clearly separated values. Later rounds should use closer comparisons.

Difficulty may eventually adapt dynamically during a run, but the initial implementation should remain understandable and deterministic.

## Artwork sources

The Metropolitan Museum of Art API is the current source.

Prefer flat artwork scans:

- Paintings
- Some collages or other genuinely flat works
- Carefully selected drawings only if they suit the visual experience

Avoid:

- Photographs of physical objects
- Sculptures
- Furniture
- Decorative objects
- Drawing-heavy imports that make the pool visually unappealing

The current Met pool emphasizes departments that provide suitable flat works, including European Paintings, the American Wing, Asian Art, and the Robert Lehman Collection.

The original sample became heavily skewed toward Italian and Netherlandish works. Future imports should improve cultural, geographic, and historical balance.

The Art Institute of Chicago API was previously attempted and caused integration problems. Do not casually reintroduce it without investigating those earlier issues.

MoMA or another modern-art source may be evaluated later to strengthen modern and contemporary coverage. Finish the Met-backed core game first.

## Current development priority

1. Complete and polish the Wager sidebar and chip animation.
2. Verify Wager economics and rake behavior.
3. Make automatic advancement reliable in Wager and Streak.
4. Test responsive behavior and artwork presentation.
5. Stabilize progressive difficulty across all implemented question strategies.
6. Only then consider additional question types or artwork sources.

The current task should remain narrowly scoped even though this document records longer-term ideas.
