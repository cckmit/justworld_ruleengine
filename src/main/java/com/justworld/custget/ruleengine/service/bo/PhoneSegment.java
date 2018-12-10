package com.justworld.custget.ruleengine.service.bo;

import java.io.Serializable;
import java.util.Date;

/**
 * phone_segment
 * @author 
 */
public class PhoneSegment implements Serializable {
    /**
     * 号段
     */
    private String segment;

    /**
     * 归属省
     */
    private String province;

    /**
     * 归属市
     */
    private String city;

    /**
     * 归属区域
     */
    private String area;

    /**
     * 1=移动，2=电信，3=联通
     */
    private String telOperator;

    /**
     * 识别方式
     */
    private String identifyType;

    /**
     * 识别时间
     */
    private Date identifyTime;

    /**
     * 状态：1=有效，2=需要重新识别
     */
    private String status;

    private static final long serialVersionUID = 1L;

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getTelOperator() {
        return telOperator;
    }

    public void setTelOperator(String telOperator) {
        this.telOperator = telOperator;
    }

    public String getIdentifyType() {
        return identifyType;
    }

    public void setIdentifyType(String identifyType) {
        this.identifyType = identifyType;
    }

    public Date getIdentifyTime() {
        return identifyTime;
    }

    public void setIdentifyTime(Date identifyTime) {
        this.identifyTime = identifyTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}