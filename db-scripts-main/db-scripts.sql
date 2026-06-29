create database auth_db;
use auth_db;
create table users(
id bigint primary key auto_increment, 
email varchar(100) not null unique, 
password varchar(255) not null,
role enum('STUDENT','WARDEN','SECURITY','ADMIN') not null,
enabled boolean not null default true,
created_at timestamp not null default current_timestamp,
updated_at timestamp null,
last_login_at timestamp null 
); 

create database outpass_db;
use outpass_db;
create table outpass(
id bigint primary key auto_increment,
student_email varchar(100) not null,
outpass_type enum('OUTING', 'OUTPASS') not null,
reason text not null,
destination varchar(100) not null,
out_time datetime not null,
expected_in_time datetime not null,
actual_out_time datetime  null,
actual_in_time datetime  null,
status enum('PENDING','PARENT_APPROVED','WARDEN_APPROVED','REJECTED','OUT','IN','CANCELLED') not null,
parent_email varchar(100) not null,
parent_approved_at datetime null,
parent_approval_token_hash varchar(255) null,
parent_approval_token_expiry datetime null,
warden_approved_at datetime null,
warden_email varchar(100) null,
warden_signature_url varchar(255) null,
warden_remarks varchar(255) null,
qr_token varchar(255),
qr_geenerated_at datetime,
created_at timestamp not null default current_timestamp,
updated_at timestamp null on update current_timestamp
);
create index idx_outpass_student on outpass(student_email);
create index idx_outpass_status on outpass(status);
create index idx_outpass_created on outpass(created_at);

