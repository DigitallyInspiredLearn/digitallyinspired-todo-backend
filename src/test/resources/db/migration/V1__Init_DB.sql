create table base_entity
(
    id bigint not null,
    primary key (id)
);

create table hibernate_sequence
(
    next_val bigint
);

insert into hibernate_sequence
values (1);

create table task
(
    id          bigint       not null,
    body        varchar(255) not null,
    is_complete bit          not null,
    todolist_id bigint,
    primary key (id)
);

create table todo_list
(
    id             bigint       not null,
    todo_list_name varchar(100) not null,
    created_by     bigint       not null,
    primary key (id)
);

create table users
(
    id       bigint not null,
    email    varchar(40),
    name     varchar(40),
    password varchar(100),
    role     integer,
    username varchar(30),
    primary key (id)
);

create table share
(
    id                 bigint not null,
    shared_user_id     bigint not null,
    shared_todolist_id bigint not null,
    primary key (id)
);

alter table share
    add constraint shared_todolist_id_fk foreign key (shared_todolist_id) references todo_list (id);

alter table task
    add constraint todolist_id_fk foreign key (todolist_id) references todo_list (id);