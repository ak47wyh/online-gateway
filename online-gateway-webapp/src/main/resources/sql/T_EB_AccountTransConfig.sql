--drop table T_EB_AccountTransConfig;
CREATE TABLE T_EB_AccountTransConfig(
	ACC_NO	VARCHAR2(30) not null,	--交易主账号
	TRANS_COMPONENT VARCHAR2(255) not null,	--组件名(类全名)
	TRANS_COMPONENT_TYPE VARCHAR2(255) not null,	--组件类别
    TRANS_ORDER NUMBER(5),--接口优先级(数值越大优先级越高)
    COMPONENT_ENABLE NUMBER(3) default 0, --是否启用接口(是否支持接口) 0,禁用; 1, 启用. 新开发添加的接口都会是禁用的, 如果旧接口过时不存在则会自动 
	CREATE_TIME TIMESTAMP(6) not null,
	UPDATE_TIME TIMESTAMP(6),
	PRIMARY KEY(ACC_NO, TRANS_COMPONENT)
);
