PRAGMA foreign_keys = ON;

CREATE TABLE users (
                       id INTEGER PRIMARY KEY,
                       username TEXT NOT NULL UNIQUE,
                       created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
);

CREATE TABLE listed_items (
                              id INTEGER PRIMARY KEY,
                              owner_id INTEGER NOT NULL,
                              name TEXT NOT NULL,
                              description TEXT,
                              cost_per_day INTEGER NOT NULL CHECK (cost_per_day >= 1),
                              status TEXT NOT NULL CHECK (
                                  status IN ('available', 'rented', 'unlisted')
                                  ),
                              created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),

                              FOREIGN KEY (owner_id)
                                  REFERENCES users(id)
                                  ON DELETE RESTRICT
);

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