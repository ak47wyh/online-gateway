--drop table T_EB_DetailQueryRecord;
CREATE TABLE T_EB_DetailQueryRecord(
  	ID int PRIMARY KEY,
  	ACC_NO  VARCHAR2(30) not null,  --交易主账号
	DETAIL_DAY TIMESTAMP(0),--明细日期
	type number(2),--类别: 0, 当日; 1, 历史
	CREATE_TIME  TIMESTAMP(6),  --创建时间
  	UPDATE_TIME  TIMESTAMP(6)  --更新时间 
);
CREATE INDEX IDX_DETAIL_QR_ACCNO on T_EB_DetailQueryRecord(ACC_NO);
CREATE SEQUENCE DETAIL_QR_ID_SEQ INCREMENT BY 1 START WITH 1 MAXVALUE 999999999999999999 NOCYCLE NOCACHE;
