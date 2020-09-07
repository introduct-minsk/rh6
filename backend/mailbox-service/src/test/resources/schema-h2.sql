drop table if exists mailbox_schema.file_info cascade;
drop table if exists mailbox_schema.message;
drop table if exists mailbox_schema.message_body;

create table mailbox_schema.message_body
(
    id   uuid default random_uuid() primary key,
    text varchar(2048)
);

create table mailbox_schema.message
(
    id                 uuid default random_uuid() primary key,
    type               varchar(12) not null,
    subject            varchar(255),
    created_on         timestamp   not null,
    unread             boolean     not null,
    related_message_id uuid references mailbox_schema.message,
    sender             varchar(13) not null,
    sender_user_id     varchar(13) not null,
    receiver           varchar(13) not null,
    body_id            uuid        references mailbox_schema.message_body ON DELETE SET NULL
);

create table mailbox_schema.file_info
(
    id             uuid default random_uuid() primary key,
    name           varchar(255) not null,
    type           varchar(255) not null,
    external       varchar(255) not null,
    attachment_for uuid references mailbox_schema.message
);

alter table mailbox_schema.message
    add sign_id uuid references mailbox_schema.file_info ON DELETE SET NULL;

