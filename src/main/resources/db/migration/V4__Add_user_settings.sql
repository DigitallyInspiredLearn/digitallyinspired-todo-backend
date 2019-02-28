create table user_settings
(
  id                                bigint not null,
  is_enable_email_notification      bit,
  is_enable_web_socket_notification bit,
  user_id                           bigint not null,
  primary key (id)
) engine = MyISAM;

alter table user_settings
  add constraint user_id unique (user_id);

alter table user_settings
  add constraint user_id_fk foreign key (user_id) references users (id)