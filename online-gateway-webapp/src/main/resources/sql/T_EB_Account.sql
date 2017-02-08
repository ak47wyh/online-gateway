--drop table T_EB_Account;
CREATE TABLE T_EB_Account(
	ACC_NO	VARCHAR2(30) primary key,	--交易主账号
	ACC_NAME NVARCHAR2(30) not null,	--交易主账户名
	BANK_NAME VARCHAR2(10) not null,	--银行简码
	BANK_FULL_NAME NVARCHAR2(30),	--客户银行全称
	BANK_BRANCH_NAME NVARCHAR2(50), --客户银行开户行全称. 如招商银行深圳高新园支行
	AREACODE CHAR(4), --开户地区号
	CNAPS VARCHAR2(15), --客户账号CNAP号
  	CURRENCY  VARCHAR2(5),  --  交易币别
	BANK_DEFAULT number(3), -- 是否为银行默认账号
	TRANS_CONFIG_ENABLE number(3) default 0, --是否启用自定义接口配置 0,禁用; 1, 启用
	CREATE_TIME TIMESTAMP(6) not null,
	UPDATE_TIME TIMESTAMP(6)	
);
 create INDEX IDX_BANK_NAME on T_EB_Account(BANK_NAME);