create table follower (
    id bigint not null,
    followed_user_id bigint not null,
    followeruser_id bigint not null,
    primary key (id)) engine=MyISAM;

alter table follower add constraint followers_fk foreign key (followeruser_id) references users (id);