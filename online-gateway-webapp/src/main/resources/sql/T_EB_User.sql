CREATE TABLE T_EB_User(
	NAME NVARCHAR2(30) PRIMARY KEY,
	REAL_NAME NVARCHAR2(30),
	PASSWORD VARCHAR2(32),
	type number(3),--管理员为1, 普通为0
	LAST_LOGIN_TIME  TIMESTAMP(6),  --最后登陆时间
	CREATE_TIME  TIMESTAMP(6),  --创建时间
  	UPDATE_TIME  TIMESTAMP(6)  --更新时间 
);

insert into t_eb_user values('admin','管理员','56e1ce3316524f219d6be2fcd88a378f', 1 ,null,sysdate,sysdate);