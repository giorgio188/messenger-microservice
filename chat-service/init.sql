CREATE USER ${DB_CHATS_USERNAME} WITH PASSWORD ${DB_CHATS_PASSWORD};
CREATE DATABASE ${DB_CHATS_NAME};
GRANT ALL PRIVILEGES ON DATABASE ${DB_CHATS_NAME} TO ${DB_CHATS_USERNAME};
ALTER USER postgres WITH PASSWORD ${POSTGRES_ADMIN_PASSWORD};

CREATE TYPE chat_type AS ENUM ('PRIVATE', 'GROUP');
CREATE TYPE chat_role AS ENUM ('CREATOR', 'ADMIN', 'MEMBER');
create type message_status as enum ('SENT', 'DELIVERED', 'READ');
create type file_type as enum ('JPEG', 'PNG', 'GIF', 'PDF', 'DOC', 'DOCX', 'TXT', 'ZIP', 'RAR', 'OTHER');

CREATE TABLE chats
(
    id          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type        chat_type NOT NULL,
    name        VARCHAR(64),
    description VARCHAR(256),
    avatar      VARCHAR(256),
    created_by  INT       NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_group_chat_name_desc CHECK (
        (type = 'GROUP' AND name IS NOT NULL) OR
        (type = 'PRIVATE' AND name IS NULL AND description IS NULL)
        )
);

CREATE TABLE chat_members
(
    id           INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    chat_id      INT NOT NULL references chats (id) on delete cascade,
    user_id      INT NOT NULL,
    role         chat_role,
    is_muted     bool      default false,
    joined_at    TIMESTAMP default CURRENT_TIMESTAMP,
    last_read_at TIMESTAMP default CURRENT_TIMESTAMP
);

CREATE TABLE chat_settings
(
    id                             INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    chat_id                        INT unique NOT NULL references chats (id) on delete cascade,
    only_admins_can_write          BOOLEAN                  DEFAULT FALSE,
    only_admins_can_add_members    BOOLEAN                  DEFAULT FALSE,
    only_admins_can_remove_members BOOLEAN                  DEFAULT FALSE,
    only_admins_can_change_info    BOOLEAN                  DEFAULT FALSE,
    created_at                     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at                     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE messages
(
    id         INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    chat_id    INT NOT NULL REFERENCES chats (id),
    sender_id  INT NOT NULL,
    content    varchar(1500),
    is_edited  boolean                  default false,
    is_deleted boolean                  default false,
    status     message_status,
    sent_at    timestamp with time zone default current_timestamp,
    edited_at  timestamp with time zone default current_timestamp
);

create table files
(
    id          int generated always as identity primary key,
    chat_id     INT     NOT NULL REFERENCES chats (id),
    message_id  int references messages (id) on delete cascade,
    sender_id   INT     NOT NULL,
    file_name   varchar not null,
    file_path   varchar not null unique,
    size        int     not null,
    type        file_type,
    is_deleted  boolean                  default false,
    uploaded_at timestamp with time zone default current_timestamp
)