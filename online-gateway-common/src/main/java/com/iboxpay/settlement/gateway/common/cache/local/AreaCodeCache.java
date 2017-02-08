package com.iboxpay.settlement.gateway.common.cache.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iboxpay.settlement.gateway.common.dao.CommonDao;
import com.iboxpay.settlement.gateway.common.dao.impl.CommonDaoImpl;
import com.iboxpay.settlement.gateway.common.domain.AreaCodeEntity;
import com.iboxpay.settlement.gateway.common.domain.BankEntity;
import com.iboxpay.settlement.gateway.common.domain.CityEntity;
import com.iboxpay.settlement.gateway.common.domain.ProvinceEntity;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

/**
 * 地区相关数据的本地缓存
 * @author jianbo_chen
 */
public class AreaCodeCache {

    private static Logger logger = LoggerFactory.getLogger(AreaCodeCache.class);

    private static CommonDao provinceDao = CommonDaoImpl.getDao(ProvinceEntity.class);
    private static CommonDao cityDao = CommonDaoImpl.getDao(CityEntity.class);
    private static CommonDao areaDao = CommonDaoImpl.getDao(AreaCodeEntity.class);
    private static CommonDao bankDao = CommonDaoImpl.getDao(BankEntity.class);

    private static List<ProvinceEntity> provinceList;
    private static List<CityEntity> cityList;
    private static List<AreaCodeEntity> areaCodeList;
    private static BankEntity[] banks;

    static {
        init();
    }

    private synchronized static void init() {
        if (provinceList == null) {
            logger.info("加载银行/地区数据...");
            long t = System.currentTimeMillis();
            List<BankEntity> bankList = bankDao.findAll();
            Collections.sort(bankList);
            AreaCodeCache.banks = bankList.toArray(new BankEntity[0]);
            List<ProvinceEntity> provinceList = provinceDao.findAll();
            List<CityEntity> cityList = cityDao.findAll();
            List<AreaCodeEntity> areaCodeList = areaDao.findAll();
            for (ProvinceEntity provinceEntity : provinceList) {//建立省市地区间关系
                for (CityEntity cityEntity : cityList) {
                    if (provinceEntity.getId() == cityEntity.getProvinceId()) {
                        if (provinceEntity.getCityList() == null) provinceEntity.setCityList(new ArrayList<CityEntity>());

                        provinceEntity.getCityList().add(cityEntity);
                        cityEntity.setProvince(provinceEntity);
                    }
                    for (AreaCodeEntity areaCodeEntity : areaCodeList) {
                        if (cityEntity.getId() == areaCodeEntity.getCityId()) {
                            if (cityEntity.getAreaCodeList() == null) cityEntity.setAreaCodeList(new ArrayList<AreaCodeEntity>());

                            cityEntity.getAreaCodeList().add(areaCodeEntity);
                            areaCodeEntity.setCity(cityEntity);
                        }
                    }
                }
            }
            AreaCodeCache.provinceList = Collections.unmodifiableList(provinceList);
            for (CityEntity cityEntity : cityList) {//对城市中的地区按地区码排序
                if (cityEntity.getAreaCodeList() != null) Collections.sort(cityEntity.getAreaCodeList());
            }
            AreaCodeCache.cityList = Collections.unmodifiableList(cityList);
            Collections.sort(areaCodeList);
            AreaCodeCache.areaCodeList = Collections.unmodifiableList(areaCodeList);
            t = System.currentTimeMillis() - t;
            logger.info("银行/地区数据加载完毕，耗时: " + t + " ms");
        }
    }

    public static List<ProvinceEntity> getProvinceList() {
        return provinceList;
    }

    public static List<CityEntity> getCityList() {
        return cityList;
    }

    public static List<AreaCodeEntity> getAreaCodeList() {
        return areaCodeList;
    }

    /**
     * 由联行号获取地区码对应城市信息
     * @param cnaps
     * @return
     */
    public static CityEntity getCityByCnaps(String cnaps) {
        AreaCodeEntity areaCodeEntity = getAreaCodeByCnaps(cnaps);
        if (areaCodeEntity != null)
            return areaCodeEntity.getCity();
        else return null;
    }

    /**
     * 由联行号获取地区码信息
     * @param cnaps
     * @return
     */
    public static AreaCodeEntity getAreaCodeByCnaps(String cnaps) {
        return getAreaCode(StringUtils.getAreaCodeFromCnaps(cnaps));
    }

    /**
     * 由地区找到市信息.地区可以从联行号中获取
     * @param areaCode
     * @return
     */
    public static CityEntity getCityByAreaCode(String areaCode) {
        AreaCodeEntity areaCodeEntity = getAreaCode(areaCode);
        if (areaCodeEntity != null)
            return areaCodeEntity.getCity();
        else return null;
    }

    /**
     * 由地区码找到地区详细信息
     * @param areaCode
     * @return
     */
    public static AreaCodeEntity getAreaCode(String areaCode) {
        if (areaCode == null) return null;
        AreaCodeEntity areaCodeEntity = new AreaCodeEntity();
        areaCodeEntity.setCode(areaCode);
        int index = Collections.binarySearch(areaCodeList, areaCodeEntity);
        if (index < 0)
            return null;
        else {
            areaCodeEntity = areaCodeList.get(index);
            return areaCodeEntity;
        }
    }

    public static BankEntity getBankByCode(String bankCode) {
        BankEntity bankEntity = new BankEntity();
        bankEntity.setCode(bankCode);
        int index = Arrays.binarySearch(banks, bankEntity);
        if (index >= 0) {
            return banks[index];
        }
        return null;
    }

    public static String getBankNameByCode(String bankCode) {
        BankEntity bankEntity = getBankByCode(bankCode);
        if (bankEntity != null) return bankEntity.getName();
        return null;
    }

}
