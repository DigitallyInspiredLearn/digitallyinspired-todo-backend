

alter table todo_list change column user_owner_id created_by bigint not null after id;
alter table todo_list add column created_date bigint not null after created_by;
alter table todo_list add column modified_by bigint after created_date;
alter table todo_list add column modified_date bigint not null after modified_by;