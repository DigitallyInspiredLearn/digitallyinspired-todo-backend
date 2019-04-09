create table tag
(
    id       bigint       not null,
    tag_name varchar(255) not null,
    owner_id bigint       not null,
    primary key (id)
) engine = MyISAM;

create table tagged_task
(
    id                 bigint not null,
    task_id            bigint not null,
    tagged_task_tag_id bigint not null,
    primary key (id)
) engine = MyISAM;

alter table tagged_task
    add constraint tagged_task_tag_id_fk foreign key (tagged_task_tag_id) references tag (id);
alter table task
    add constraint todolist_id_fk foreign key (todolist_id) references todo_list (id);

