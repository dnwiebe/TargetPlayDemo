-- !Ups

create table games (
  id bigint auto_increment primary key,
  timestamp bigint not null,
  winner bigint null
);

create table players (
  id bigint auto_increment primary key,
  name varchar not null
);

create table hits (
  id bigint auto_increment primary key,
  timestamp bigint not null,
  game_id bigint not null,
  player_id bigint not null,
  points int not null
);

-- !Downs

drop table hits;
drop table players;
drop table games;
