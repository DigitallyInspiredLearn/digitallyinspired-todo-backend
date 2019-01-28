create table follower (
    id bigint not null,
    follower_user_id bigint not null,
    followeduser_id bigint not null,
    primary key (id)) engine=MyISAM;

alter table follower add constraint follow_fk foreign key (followeduser_id) references users (id);