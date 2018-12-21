package com.justworld.custget.ruleengine.service.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * sms_click_log
 * @author 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmsClickLog implements Serializable {
    private Long id;

    private Date clickTime;

    /**
     * 短信ID
     */
    private Long smsId;

    private static final long serialVersionUID = 1L;
}