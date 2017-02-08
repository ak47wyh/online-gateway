--drop table T_EB_AccountExt;
CREATE TABLE T_EB_AccountExt(
	ACC_NO VARCHAR2(30) not null,-- 账号
	NAME varchar2(20) not null,-- 扩展属性名 
	VALUE nvarchar2(100), --扩展属性值
	primary key(ACC_NO, NAME)
);
 