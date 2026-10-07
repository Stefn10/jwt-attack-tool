CREATE TYPE user_type AS ENUM ('ADMIN', 'CREATOR');
CREATE TYPE reaction_type AS ENUM ('LIKE', 'DISLIKE');

CREATE TABLE users (
    user_id   BIGSERIAL    PRIMARY KEY,
    email     VARCHAR(255) NOT NULL UNIQUE,
    name      VARCHAR(100) NOT NULL,
    surname   VARCHAR(100) NOT NULL,
    user_type user_type    NOT NULL,
    status    BOOLEAN      NOT NULL DEFAULT TRUE,
    password  VARCHAR(64)  NOT NULL  -- SHA-256 hex = 64 karaktera
);

CREATE TABLE categories (
    category_id BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL
);

CREATE TABLE news (
    news_id          BIGSERIAL    PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    text             TEXT         NOT NULL,
    published_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    number_of_visits INTEGER      NOT NULL DEFAULT 0,
    author_id        BIGINT       NOT NULL REFERENCES users(user_id),
    category_id      BIGINT       NOT NULL REFERENCES categories(category_id)
);
CREATE INDEX idx_news_category ON news(category_id);
CREATE INDEX idx_news_author   ON news(author_id);
CREATE INDEX idx_news_pubtime  ON news(published_time DESC);

CREATE TABLE tags (
    tag_id BIGSERIAL    PRIMARY KEY,
    value  VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE news_tags (
    news_id BIGINT NOT NULL REFERENCES news(news_id) ON DELETE CASCADE,
    tag_id  BIGINT NOT NULL REFERENCES tags(tag_id)  ON DELETE CASCADE,
    PRIMARY KEY (news_id, tag_id)
);

CREATE TABLE comments (
    comment_id  BIGSERIAL    PRIMARY KEY,
    news_id     BIGINT       NOT NULL REFERENCES news(news_id) ON DELETE CASCADE,
    author_name VARCHAR(100) NOT NULL,
    text        TEXT         NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_comments_news ON comments(news_id, created_at DESC);

CREATE TABLE news_visits (
    visit_id   BIGSERIAL   PRIMARY KEY,
    news_id    BIGINT      NOT NULL REFERENCES news(news_id) ON DELETE CASCADE,
    session_id VARCHAR(64) NOT NULL,
    visited_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (news_id, session_id)
);
CREATE INDEX idx_news_visits_recent ON news_visits(visited_at);

CREATE TABLE news_reactions (
    reaction_id   BIGSERIAL     PRIMARY KEY,
    news_id       BIGINT        NOT NULL REFERENCES news(news_id) ON DELETE CASCADE,
    session_id    VARCHAR(64)   NOT NULL,
    reaction_type reaction_type NOT NULL,
    UNIQUE (news_id, session_id)
);

CREATE TABLE comment_reactions (
    reaction_id   BIGSERIAL     PRIMARY KEY,
    comment_id    BIGINT        NOT NULL REFERENCES comments(comment_id) ON DELETE CASCADE,
    session_id    VARCHAR(64)   NOT NULL,
    reaction_type reaction_type NOT NULL,
    UNIQUE (comment_id, session_id)
);

INSERT INTO users (email, name, surname, user_type, status, password)
VALUES ('admin@raf.rs', 'Admin', 'Admin', 'ADMIN', TRUE,
        '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918');
