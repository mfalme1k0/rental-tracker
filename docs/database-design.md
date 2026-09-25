# Database design  (owner: Fidel)

_Stub created in Phase 0 so the file exists and PRs only add content. Replace this text._

Must document (spec deliverable, plus the ER diagram `er-diagram.png` next to this file):

- [ ] The three tables (`users`, `listed_items`, `rentals`): every column, type, constraint
- [ ] Relationships (owner -> items, item -> rentals, renter -> rentals)
- [ ] Why each constraint exists (NOT NULL, UNIQUE, CHECK, FOREIGN KEY, partial unique index)
- [ ] Timestamp format and time zone (see architecture.md D9)
- [ ] How `schema.sql` is applied on start-up
- [ ] How the seeded `data/rental-tracker.db` (>= 6 listed items) is produced
