--drop table T_EB_FrontEndProperty;
CREATE TABLE T_EB_FrontEndProperty(
	ID INT PRIMARY KEY,
	PARENT_ID INT NOT NULL, --所属的前置机配置ID
	NAME VARCHAR2(64) not null,
	VALUE NVARCHAR2(100)
);
 CREATE SEQUENCE FRONTENDPRO_ID_SEQ INCREMENT BY 1 START WITH 1 MAXVALUE 999999999999999999 NOCYCLE NOCACHE;
