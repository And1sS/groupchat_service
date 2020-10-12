CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS group_chat
(
    id         UUID DEFAULT uuid_generate_v4(),
    title      TEXT NOT NULL,
    about      TEXT,
    creator_id UUID,

    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS group_message
(
    id            UUID DEFAULT uuid_generate_v4(),
    author_id     UUID,
    chat_id       UUID NOT NULL,
    contents      TEXT NOT NULL,
    creation_time TIMESTAMP DEFAULT NOW(),

    PRIMARY KEY (id),
    CONSTRAINT chat_id_constraint FOREIGN KEY (chat_id) REFERENCES group_chat (id) ON DELETE CASCADE
);


CREATE TYPE MemberType AS ENUM ('read', 'readwrite', 'admin');
CREATE CAST (character varying AS MemberType) WITH INOUT AS ASSIGNMENT;

CREATE TABLE IF NOT EXISTS group_user
(
    group_chat_id UUID       NOT NULL,
    user_id       UUID       NOT NULL,
    member_type   MemberType NOT NULL,

    PRIMARY KEY (group_chat_id, user_id),
    CONSTRAINT group_chat_id_constraint FOREIGN KEY (group_chat_id) REFERENCES group_chat (id) ON DELETE CASCADE
);