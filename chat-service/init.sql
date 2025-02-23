CREATE TYPE chat_type AS ENUM ('PRIVATE', 'GROUP');
CREATE TYPE chat_role AS ENUM ('CREATOR', 'ADMIN', 'MEMBER');

CREATE TABLE chats
(
    id          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type        chat_type NOT NULL,
    name        VARCHAR(64),
    description VARCHAR(256),
    avatar      VARCHAR(255),
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
    id        INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    chat_id   INT NOT NULL references chats (id) on delete cascade,
    user_id   INT NOT NULL,
    role      chat_role,
    is_muted bool default false,
    joined_at TIMESTAMP default CURRENT_TIMESTAMP,
    last_read_at TIMESTAMP default CURRENT_TIMESTAMP
);

CREATE TABLE chat_settings (
                               id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               chat_id INT unique NOT NULL references chats(id) on delete cascade,
                               only_admins_can_write BOOLEAN DEFAULT FALSE,
                               only_admins_can_add_members BOOLEAN DEFAULT FALSE,
                               only_admins_can_remove_members BOOLEAN DEFAULT FALSE,
                               only_admins_can_change_info BOOLEAN DEFAULT FALSE,
                               created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);