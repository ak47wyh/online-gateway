package com.iboxpay.settlement.gateway.common.domain;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "T_EB_CITY")
public class CityEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID")
    private int id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "PROVINCE_ID")
    private int provinceId;

    private transient ProvinceEntity province;

    private transient List<AreaCodeEntity> areaCodeList;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }

    public void setProvince(ProvinceEntity province) {
        this.province = province;
    }

    public ProvinceEntity getProvince() {
        return province;
    }

    public List<AreaCodeEntity> getAreaCodeList() {
        return areaCodeList;
    }

    public void setAreaCodeList(List<AreaCodeEntity> areaCodeList) {
        this.areaCodeList = areaCodeList;
    }
}
