create table mailbox_schema.file_info
(
    id       uuid primary key default uuid_generate_v4(),
    name     varchar(255) not null,
    type     varchar(255) not null,
    external varchar(255) not null
);

alter table mailbox_schema.message
    add sign_id uuid references mailbox_schema.file_info ON DELETE SET NULL;
