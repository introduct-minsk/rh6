alter table mailbox_schema.file_info
    add attachment_for uuid references mailbox_schema.message;

