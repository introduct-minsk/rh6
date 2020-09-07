create index message_receiver_index
    on mailbox_schema.message (receiver);

create index message_sender_index
    on mailbox_schema.message (sender);
