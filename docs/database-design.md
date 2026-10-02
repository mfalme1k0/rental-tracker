# Database Design

**Owner:** Fidel

## 1. Overview

Rental Tracker uses SQLite as its relational database.

The database schema is defined in:

```text
src/main/resources/schema.sql
```

Database access is handled through JDBC repositories in:

```text
src/main/java/com/rentaltracker/repository/sqlite/
```

Connections are created by `DatabaseManager`.

SQLite foreign-key enforcement is explicitly enabled for every database connection.

The database contains three tables:

* `users`
* `listed_items`
* `rentals`

The relationships between these tables represent users who own listed items, users who rent those items, and the rental history of each item.

---

## 2. Entity Relationship Diagram

The database relationships are also represented in:

```text
docs/er-diagram.png
```

The main relationships are:

```text
users
  │
  ├──< listed_items
  │       │
  │       └──< rentals
  │
  └──< rentals
```

A user can own many listed items.

A listed item can have many rental records over its lifetime.

A user can also have many rentals as a renter.

---

## 3. `users`

The `users` table stores users of the application.

| Column       | SQLite type | Constraints           | Purpose                            |
| ------------ | ----------- | --------------------- | ---------------------------------- |
| `id`         | `INTEGER`   | `PRIMARY KEY`         | Database-generated user identifier |
| `username`   | `TEXT`      | `NOT NULL`, `UNIQUE`  | User's unique username             |
| `created_at` | `TEXT`      | `NOT NULL`, `DEFAULT` | Creation timestamp stored in UTC   |

### Schema

```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    created_at TEXT NOT NULL
        DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
);
```

### Constraints

#### `PRIMARY KEY`

`id` uniquely identifies each user.

SQLite generates the identifier when a new user is inserted.

#### `NOT NULL`

`username` must exist because a user cannot be identified without a username.

`created_at` must also exist because every persisted user needs a creation timestamp.

#### `UNIQUE`

`username` must be unique so that two users cannot have the same username.

---

## 4. `listed_items`

The `listed_items` table stores items that users have listed for rental.

| Column         | SQLite type | Constraints               | Purpose                            |
| -------------- | ----------- | ------------------------- | ---------------------------------- |
| `id`           | `INTEGER`   | `PRIMARY KEY`             | Database-generated item identifier |
| `owner_id`     | `INTEGER`   | `NOT NULL`, `FOREIGN KEY` | User who owns the item             |
| `name`         | `TEXT`      | `NOT NULL`                | Item name                          |
| `description`  | `TEXT`      | Nullable                  | Optional item description          |
| `cost_per_day` | `TEXT`      | `NOT NULL`, `CHECK`       | Daily rental cost                  |
| `status`       | `TEXT`      | `NOT NULL`, `CHECK`       | Current listing status             |
| `created_at`   | `TEXT`      | `NOT NULL`, `DEFAULT`     | Creation timestamp stored in UTC   |

### Schema

```sql
CREATE TABLE listed_items (
    id INTEGER PRIMARY KEY,
    owner_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    cost_per_day TEXT NOT NULL CHECK (
        cost_per_day NOT GLOB '*[^0-9.]*'
        AND length(cost_per_day) > 0
        AND length(cost_per_day)
            - length(replace(cost_per_day, '.', '')) <= 1
        AND substr(cost_per_day, 1, 1) GLOB '[0-9]'
        AND substr(cost_per_day, -1, 1) GLOB '[0-9]'
        AND replace(
            replace(cost_per_day, '0', ''),
            '.',
            ''
        ) <> ''
    ),
    status TEXT NOT NULL CHECK (
        status IN ('available', 'rented', 'unlisted')
    ),
    created_at TEXT NOT NULL
        DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),

    FOREIGN KEY (owner_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
);
```

### `owner_id`

`owner_id` references `users.id`.

This ensures that every listed item belongs to an existing user.

`ON DELETE RESTRICT` prevents a user from being deleted while they still own listed items.

### `cost_per_day`

The domain represents rental costs using Java `BigDecimal`.

SQLite does not provide a dedicated decimal type, and SQLite `REAL` uses floating-point representation. To avoid losing decimal precision, `cost_per_day` is stored as `TEXT`.

The repository converts:

```text
BigDecimal
    ↓
toPlainString()
    ↓
SQLite TEXT
```

and converts it back when reading:

```text
SQLite TEXT
    ↓
BigDecimal
```

The database `CHECK` constraint ensures that the stored value has a valid decimal-number format and represents a positive value.

This provides database-level protection against invalid monetary values even if Java-side validation is bypassed.

### `status`

The database permits only these values:

```text
available
rented
unlisted
```

The `CHECK` constraint prevents arbitrary status values from being stored.

The Java `ItemStatus` enum maps to these database values.

The legality of transitions between statuses is handled by the domain model rather than by the database.

---

## 5. `rentals`

The `rentals` table stores rental transactions and rental history.

| Column        | SQLite type | Constraints               | Purpose                              |
| ------------- | ----------- | ------------------------- | ------------------------------------ |
| `id`          | `INTEGER`   | `PRIMARY KEY`             | Database-generated rental identifier |
| `item_id`     | `INTEGER`   | `NOT NULL`, `FOREIGN KEY` | Item being rented                    |
| `renter_id`   | `INTEGER`   | `NOT NULL`, `FOREIGN KEY` | User renting the item                |
| `start_time`  | `TEXT`      | `NOT NULL`                | Rental start timestamp               |
| `end_time`    | `TEXT`      | `NOT NULL`, `CHECK`       | Rental end timestamp                 |
| `returned_at` | `TEXT`      | Nullable                  | Time the item was returned           |
| `status`      | `TEXT`      | `NOT NULL`, `CHECK`       | Rental status                        |

### Schema

```sql
CREATE TABLE rentals (
    id INTEGER PRIMARY KEY,
    item_id INTEGER NOT NULL,
    renter_id INTEGER NOT NULL,
    start_time TEXT NOT NULL,
    end_time TEXT NOT NULL,
    returned_at TEXT,
    status TEXT NOT NULL CHECK (
        status IN ('active', 'closed')
    ),
    CHECK (end_time > start_time),

    FOREIGN KEY (item_id)
        REFERENCES listed_items(id)
        ON DELETE RESTRICT,

    FOREIGN KEY (renter_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
);
```

### `item_id`

`item_id` references `listed_items.id`.

This prevents a rental from referring to an item that does not exist.

`ON DELETE RESTRICT` prevents an item from being deleted while it has rental records.

Rental records form part of an item's history and should not disappear simply because an item is no longer listed.

### `renter_id`

`renter_id` references `users.id`.

This ensures that every rental has an existing user as the renter.

`ON DELETE RESTRICT` prevents a user from being deleted while they are referenced by rental records.

### `start_time` and `end_time`

Both timestamps are required.

The database enforces:

```sql
CHECK (end_time > start_time)
```

This prevents zero-length and negative-length rentals at the database level.

The Java application also validates rental duration before persistence.

### `returned_at`

`returned_at` is nullable because an active rental has not yet been returned.

For an active rental:

```text
status = active
returned_at = NULL
```

For a closed rental:

```text
status = closed
returned_at = return timestamp
```

### `status`

The database permits only:

```text
active
closed
```

The `CHECK` constraint prevents invalid rental states from being stored.

The Java `RentalStatus` enum maps to these database values.

---

## 6. One Active Rental Per Item

The database contains a partial unique index:

```sql
CREATE UNIQUE INDEX idx_rentals_one_active_per_item
ON rentals(item_id)
WHERE status = 'active';
```

This enforces the rule that an item can have at most one active rental at a time.

This is a partial unique index because uniqueness applies only to rows where:

```text
status = 'active'
```

Multiple historical `closed` rentals for the same item are therefore allowed.

For example, this is valid:

```text
item 10 → rental 1 → closed
item 10 → rental 2 → closed
item 10 → rental 3 → active
```

But this is rejected:

```text
item 10 → rental 1 → active
item 10 → rental 2 → active
```

The database therefore enforces the invariant rather than relying only on application logic.

---

## 7. Relationships

### User → Listed Items

One user can own many listed items.

```text
users.id
    │
    └──< listed_items.owner_id
```

The relationship is implemented by:

```sql
FOREIGN KEY (owner_id)
REFERENCES users(id)
ON DELETE RESTRICT
```

### Listed Item → Rentals

One listed item can have many rental records over its lifetime.

```text
listed_items.id
    │
    └──< rentals.item_id
```

The relationship is implemented by:

```sql
FOREIGN KEY (item_id)
REFERENCES listed_items(id)
ON DELETE RESTRICT
```

### User → Rentals as Renter

One user can have many rentals.

```text
users.id
    │
    └──< rentals.renter_id
```

The relationship is implemented by:

```sql
FOREIGN KEY (renter_id)
REFERENCES users(id)
ON DELETE RESTRICT
```

The `users` table therefore participates in two different relationships involving rentals:

* as the owner of the rented item through `listed_items.owner_id`
* as the renter through `rentals.renter_id`

---

## 8. Constraint Summary

| Constraint           | Table                              | Purpose                                            |
| -------------------- | ---------------------------------- | -------------------------------------------------- |
| `PRIMARY KEY`        | `users`, `listed_items`, `rentals` | Uniquely identifies each record                    |
| `NOT NULL`           | Required columns                   | Prevents required data from being absent           |
| `UNIQUE`             | `users.username`                   | Prevents duplicate usernames                       |
| `FOREIGN KEY`        | `listed_items.owner_id`            | Ensures every item has an existing owner           |
| `FOREIGN KEY`        | `rentals.item_id`                  | Ensures every rental references an existing item   |
| `FOREIGN KEY`        | `rentals.renter_id`                | Ensures every rental references an existing renter |
| `ON DELETE RESTRICT` | Foreign keys                       | Prevents deletion of referenced records            |
| `CHECK`              | `listed_items.cost_per_day`        | Prevents invalid/non-positive monetary values      |
| `CHECK`              | `listed_items.status`              | Restricts item status to known values              |
| `CHECK`              | `rentals.status`                   | Restricts rental status to known values            |
| `CHECK`              | `rentals.end_time`                 | Requires the rental to have a positive duration    |
| Partial unique index | `rentals.item_id`                  | Allows at most one active rental per item          |

---

## 9. Timestamp Format and Time Zone

Timestamps are stored as SQLite `TEXT`.

Database-generated timestamps use:

```sql
strftime('%Y-%m-%dT%H:%M:%fZ', 'now')
```

This produces an ISO-8601 UTC timestamp such as:

```text
2026-09-30T08:15:42.123Z
```

The `Z` indicates UTC.

Application timestamps are treated as UTC before being persisted by the SQLite repositories.

When timestamps are read from SQLite, they are parsed from the stored ISO-8601 representation back into Java date/time types.

Using a consistent UTC representation avoids storing different local time zones in different records and makes timestamp comparison and ordering predictable.

---

## 10. Schema Initialization

The database schema is applied by `DatabaseManager.initialize()`.

The initialization process is:

```text
DatabaseManager.initialize()
        │
        ▼
getConnection()
        │
        ▼
PRAGMA foreign_keys = ON
        │
        ▼
Load /schema.sql from the classpath
        │
        ▼
Execute schema SQL
        │
        ▼
Tables and indexes are created
```

The schema file is loaded using:

```java
DatabaseManager.class.getResourceAsStream("/schema.sql")
```

The SQL is read as UTF-8 and executed against the SQLite connection.

Foreign-key enforcement is explicitly enabled whenever a connection is created:

```sql
PRAGMA foreign_keys = ON;
```

This is necessary because SQLite foreign-key enforcement must be enabled for each connection.

---

## 11. Seed Database

The development seed database is located at:

```text
data/rental-tracker.db
```

The seed database must contain at least six listed items.

The seed database is based on the same schema defined in:

```text
src/main/resources/schema.sql
```

The seed data must satisfy all database constraints, including:

* valid user references
* valid item ownership
* valid rental references
* valid item statuses
* valid rental statuses
* valid positive rental costs
* valid rental time ranges
* the one-active-rental-per-item rule

The intended process for producing the seed database is:

1. Create a fresh SQLite database.
2. Apply `src/main/resources/schema.sql`.
3. Insert valid users.
4. Insert at least six listed items.
5. Insert any required rental/demo records.
6. Verify that all database constraints pass.
7. Save the resulting database as:

```text
data/rental-tracker.db
```

The seeded database is development/demo data.

`schema.sql` remains the authoritative definition of the database structure and constraints.

---

## 12. ER Diagram

The visual ER diagram is stored next to this document:

```text
docs/er-diagram.png
```

The diagram represents the three tables and their foreign-key relationships.

The `users` entity participates in rentals in two different roles:

* owner of the rented item
* renter of the item
