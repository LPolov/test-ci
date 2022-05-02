CREATE TABLE IF NOT EXISTS users
(
    id              VARCHAR(255),
    email           VARCHAR(255)    NOT NULL,
    enabled         BIT             NOT NULL DEFAULT 0,
    first_name      VARCHAR(50)    NOT NULL,
    last_name       VARCHAR(50)    NOT NULL,
    password        VARCHAR(255)    NOT NULL,
    role            VARCHAR(255)    NOT NULL
);
ALTER TABLE users ADD CONSTRAINT users_pkey PRIMARY KEY (id);

CREATE TABLE IF NOT EXISTS products
(
    id          VARCHAR(255),
    name        VARCHAR(255)    NOT NULL,
    proteins    DECIMAL(5,1)    NOT NULL,
    fats        DECIMAL(5,1)    NOT NULL,
    carbs       DECIMAL(5,1)    NOT NULL,
    calories    DECIMAL(6,1)    NOT NULL
    );
ALTER TABLE products ADD CONSTRAINT products_pkey PRIMARY KEY (id);

CREATE TABLE IF NOT EXISTS daily_cuts
(
    id          VARCHAR(255),
    calories    DECIMAL(6,1)    NOT NULL,
    proteins    DECIMAL(5,1)    NOT NULL,
    fats        DECIMAL(5,1)    NOT NULL,
    carbs       DECIMAL(5,1)    NOT NULL,
    weight      DECIMAL(4,1)    NOT NULL,
    cut_date    timestamp       NOT NULL
    );
ALTER TABLE daily_cuts ADD CONSTRAINT daily_cuts_pkey PRIMARY KEY (id);

CREATE TABLE IF NOT EXISTS user_progress
(
    id              VARCHAR(255),
    user_id         VARCHAR(255)    NOT NULL,
    daily_cut_id    VARCHAR(255)    NOT NULL
    );
ALTER TABLE user_progress ADD CONSTRAINT user_progress_pkey PRIMARY KEY (id);
ALTER TABLE user_progress ADD CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE user_progress ADD CONSTRAINT fk_daily_cut_id FOREIGN KEY (daily_cut_id) REFERENCES daily_cuts(id) ON DELETE CASCADE;