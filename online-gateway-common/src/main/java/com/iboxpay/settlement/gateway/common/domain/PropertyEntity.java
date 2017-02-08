package com.iboxpay.settlement.gateway.common.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "T_EB_ConfProperty")
public class PropertyEntity implements Serializable, Comparable<PropertyEntity> {

    private static final long serialVersionUID = 1L;

    @Id
    private Pk pk;

    @Column(name = "VALUE")
    private String value;//nvarchar2(100)

    @Column(name = "CREATE_TIME")
    private Date createTime;//	TIMESTAMP(6)	N

    @Column(name = "UPDATE_TIME")
    private Date updateTime;//	TIMESTAMP(6)	N	

    static Pattern p = Pattern.compile("(.*)\\[(\\d+)\\]");

    public Pk getPk() {
        return pk;
    }

    public void setPk(Pk pk) {
        this.pk = pk;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public int compareTo(PropertyEntity o) {
        return this.pk.compareTo(o.pk);
    }

    @Override
    public String toString() {
        return this.pk.owner + " - " + this.pk.name + (this.pk.index != -1 ? "[" + this.pk.index + "]" : "") + " - " + value;
    }

    @Embeddable
    public static class Pk implements Serializable, Comparable<Pk> {

        private static final long serialVersionUID = 1L;

        @Column(name = "OWNER")
        private String owner;//char(10)

        @Column(name = "NAME")
        private String name;//varchar2(20)

        private transient String realName;//去掉[0]的
        private transient int index = -1;

        public Pk() {

        }

        public Pk(String owner, String name) {
            this.owner = owner;
            _setName(name);
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public String getName() {
            return name;
        }

        public String getRealName() {
            return realName;
        }

        public void setName(String name) {
            _setName(name);
        }

        private void _setName(String name) {
            if (name.indexOf('[') != -1) {
                Matcher matcher = p.matcher(name);
                if (matcher.matches()) {
                    this.index = Integer.parseInt(matcher.group(2));
                    this.realName = matcher.group(1);
                } else {
                    throw new RuntimeException("invalid name : " + name);
                }
            }
            if (realName == null) realName = name;

            this.name = name;
        }

        @Override
        public int compareTo(Pk o) {
            if (this.realName == null) _setName(name);
            if (o.realName == null) o._setName(o.name);

            int r = this.owner.compareTo(o.owner);
            if (r == 0) {
                r = this.realName.compareTo(o.realName);
                if (r == 0)
                    return this.index - o.index;
                else return r;
            } else {
                return r;
            }
        }

        @Override
        public boolean equals(Object obj) {
            Pk pk = (Pk) obj;
            return this.owner.equals(pk.owner) && this.name.equals(pk.name);
        }

        public int hashCode() {
            return new StringBuilder().append(this.owner).append("_").append(this.name).toString().hashCode();
        }
    }
}
