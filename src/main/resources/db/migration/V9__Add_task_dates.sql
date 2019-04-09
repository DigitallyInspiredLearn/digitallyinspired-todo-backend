alter table task add column created_by varchar(40) not null after is_complete;
alter table task add column created_date bigint not null after created_by;
alter table task add column completed_date bigint after created_date;
alter table task add column duration_time bigint after completed_date;