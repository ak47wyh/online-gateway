--drop table T_EB_Sequence;
CREATE TABLE T_EB_Sequence(
  	KEY VARCHAR2(30) PRIMARY KEY, --序列号键
  	SEQ NUMBER(19), --序列号
	CREATE_TIME TIMESTAMP(6), 	
	UPDATE_TIME TIMESTAMP(6)
);
 
 