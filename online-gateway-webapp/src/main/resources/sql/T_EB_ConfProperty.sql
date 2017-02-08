--drop table T_EB_ConfProperty;
CREATE TABLE T_EB_ConfProperty(
	OWNER varchar2(10) not null,
	NAME varchar2(20) not null,
	VALUE nvarchar2(100),
	CREATE_TIME TIMESTAMP(6) not null,
	UPDATE_TIME TIMESTAMP(6),
	primary key(OWNER, NAME)
);
