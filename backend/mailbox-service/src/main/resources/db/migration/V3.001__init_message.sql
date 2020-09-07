create schema if not exists mailbox_schema;

create extension if not exists "uuid-ossp";

create table mailbox_schema.message_body
(
    id   uuid primary key default uuid_generate_v4(),
    text varchar(2048)
);

create table mailbox_schema.message
(
    id                 uuid primary key default uuid_generate_v4(),
    type               varchar(12) not null,
    subject            varchar(255),
    created_on         timestamp   not null,
    unread             boolean     not null,
    related_message_id uuid references mailbox_schema.message,
    sender             varchar(13) not null,
    sender_role        varchar(13) not null,
    receiver           varchar(13) not null,
    body_id            uuid        references mailbox_schema.message_body ON DELETE SET NULL
);
