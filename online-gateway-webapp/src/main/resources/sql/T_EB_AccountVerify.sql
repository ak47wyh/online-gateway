--drop table T_EB_AccountVerify;
-- Create table
create table T_EB_ACCOUNTVERIFY
(
  ID                 INTEGER PRIMARY KEY not null,
  CUSTOMER_ACC_NO    VARCHAR2(30),  --客户账号
  CUSTOMER_ACC_NAME  NVARCHAR2(30), --客户账户名
  CUSTOMER_ACC_TYPE  NUMBER(3),     --账户类型(1-表示对公 2-表示对私 3-表示对私存折)
  CUSTOMER_CARD_TYPE NUMBER(3),     --卡类型(0-存折 1-借记卡 2-贷记卡)
  CERT_NO            NVARCHAR2(20), --证件号码
  REMARK             NVARCHAR2(100),--备注
  STATUS             NUMBER(3),     --交易状态
  ERROR_CODE         NVARCHAR2(20), --返回的错误码
  ERROR_MSG          NVARCHAR2(100),--返回的错误码解析
  SEQ_ID             NVARCHAR2(50), --序列号
  CREATE_TIME        TIMESTAMP(6),  --创建时间
  UPDATE_TIME        TIMESTAMP(6),  --更新时间 
  MOBILE_NO          NVARCHAR2(12), --手机号
  SYS_NAME           NVARCHAR2(10), --验证来源
  BANK_STATUS        NVARCHAR2(30), --银行状态
  VERIFY_ERROR_CODE  NVARCHAR2(20), --验证错误编码
  PAY_TRANS_CODE     VARCHAR2(32),  --
  BATCH_SEQ_ID       VARCHAR2(32),  --批次号
  STATUS_MSG         NVARCHAR2(100),--状态描述
  BANK_BATCH_SEQ_ID  NVARCHAR2(20), --银行批次号
  BANK_STATUS_MSG    NVARCHAR2(100),--银行状态描述
  EXT_PROPERTIES     NVARCHAR2(1000),--扩展属性
  CERT_TYPE          NVARCHAR2(10),  --证件类型
  BANK_SEQ_ID        NVARCHAR2(20)   --银行流水号
)
CREATE SEQUENCE ACCOUNTVERIFY_ID_SEQ INCREMENT BY 1 START WITH 91 MAXVALUE 999999999999999999 NOCYCLE NOCACHE;
