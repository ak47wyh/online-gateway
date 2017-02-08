package com.iboxpay.settlement.gateway.common.domain;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "T_EB_PROVINCE")
public class ProvinceEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID")
    private int id;

    @Column(name = "NAME")
    private String name;

    private transient List<CityEntity> cityList;

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

    public void setCityList(List<CityEntity> cityList) {
        this.cityList = cityList;
    }

    public List<CityEntity> getCityList() {
        return cityList;
    }
}
