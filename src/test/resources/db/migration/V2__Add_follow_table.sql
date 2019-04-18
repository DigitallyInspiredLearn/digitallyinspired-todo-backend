create table follower
(
    id               bigint not null,
    followed_user_id bigint not null,
    follower_user_id bigint not null,
    primary key (id)
);

alter table follower
    add constraint followers_fk foreign key (follower_user_id) references users (id);