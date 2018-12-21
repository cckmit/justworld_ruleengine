package com.justworld.custget.ruleengine.service.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * sms_click_stat
 * @author 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmsClickStat implements Serializable {
    private Long id;

    /**
     * 最后点击时间
     */
    private Date lastClickTime;

    /**
     * 点击次数
     */
    private Integer clickCount;

    private static final long serialVersionUID = 1L;
}