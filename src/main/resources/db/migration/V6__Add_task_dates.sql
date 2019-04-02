
alter table task add column created_date bigint not null after is_complete;
alter table task add column completed_date bigint after created_date;
alter table task add column realization_time bigint after completed_date;