create table tag
(
    id       bigint       not null,
    tag_name varchar(255) not null,
    owner_id bigint       not null,
    primary key (id)
) engine = MyISAM;

create table tag_task_key
(
    id                 bigint not null,
    task_id            bigint not null,
    tag_id bigint not null,
    primary key (id)
) engine = MyISAM;

alter table tag_task_key
    add constraint tag_id_fk foreign key (tag_id) references tag (id);
alter table task
    add constraint todolist_id_fk foreign key (todolist_id) references todo_list (id);
alter table tag
    add column color varchar(20) not null after owner_id;

