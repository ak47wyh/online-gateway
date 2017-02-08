package com.iboxpay.settlement.gateway.common.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "T_EB_FrontEndProperty")
public class FrontEndPropertyEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FrontEndProp_generator")
    @SequenceGenerator(name = "FrontEndProp_generator", sequenceName = "FRONTENDPRO_ID_SEQ", allocationSize = 20, initialValue = 1)
    @Column(name = "ID")
    private Integer id;//	ID INT PRIMARY KEY,

    @Column(name = "PARENT_ID")
    private Integer parentId;//	PARENT_ID INT NOT NULL, --所属的前置机配置ID

    @Column(name = "NAME")
    private String name;//	NAME VARCHAR2(20) not null,

    @Column(name = "VALUE")
    private String value;//	VALUE NVARCHAR2(100)

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
