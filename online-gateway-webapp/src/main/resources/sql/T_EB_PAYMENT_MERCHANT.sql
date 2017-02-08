create table T_EB_PAYMENT_MERCHANT(
  ID INTEGER not null,
	APP_CODE VARCHAR2(32),  --交易主账号
	APP_ID VARCHAR2(32),    --公众号
	APP_SECRET VARCHAR2(64),--公众号密钥
	SUB_APP_ID VARCHAR2(32),--子公众号编号
	SUB_APP_SECRET VARCHAR2(64),--子公众号密钥
	PAY_MERCHANT_NAME VARCHAR2(128), --交易商户名称
	PAY_MERCHANT_NO VARCHAR2(32),    --交易商户号
	PAY_MERCHANT_SUB_NO VARCHAR2(32),--子商户号/代理商编号
	PAY_MERCHANT_KEY VARCHAR2(64)    --商户秘钥
);
CREATE SEQUENCE PAYMENT_MERCHANT_ID_SEQ INCREMENT BY 1 START WITH 1 MAXVALUE 999999999999999999 NOCYCLE NOCACHE;