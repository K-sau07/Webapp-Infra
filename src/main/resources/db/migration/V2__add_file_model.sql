CREATE TABLE IF NOT EXISTS file (
    id          TEXT        NOT NULL PRIMARY KEY,
    file_name   TEXT        NOT NULL,
    url         TEXT        NOT NULL,
    upload_date TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id     TEXT        NOT NULL
);
