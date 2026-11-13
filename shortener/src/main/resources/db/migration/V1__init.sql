create table users (
    id bigserial primary key,
    email varchar(255) not null unique,
    password_hash varchar(255) not null
);

create table short_links (
    id bigserial primary key,
    code varchar(16) not null unique,
    long_url text not null,
    owner_id bigint not null references users (id),
    clicks bigint not null default 0
);

create index short_links_owner_idx on short_links (owner_id);
