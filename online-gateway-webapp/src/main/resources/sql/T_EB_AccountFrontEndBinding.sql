--drop table T_EB_AccountFrontEndBinding;
CREATE TABLE T_EB_AccountFrontEndBinding(
	ACC_NO	VARCHAR2(30) not null,--公司账号
	FRONT_END_ID int not null,--前置机配置ID
	CREATE_TIME TIMESTAMP(6) not null,
	UPDATE_TIME TIMESTAMP(6),
	primary key(ACC_NO, FRONT_END_ID)
);
