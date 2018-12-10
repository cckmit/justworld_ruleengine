package com.justworld.custget.ruleengine.dao;

import com.justworld.custget.ruleengine.service.bo.PhoneSegment;
import org.apache.ibatis.annotations.Insert;
import org.springframework.stereotype.Repository;

@Repository
public interface PhoneSegmentDAO {
    int deleteByPrimaryKey(String segment);

    int insert(PhoneSegment record);

    @Insert("INSERT INTO `phone_segment` (\n" +
            "  `SEGMENT`, `PROVINCE`, `CITY`, `AREA`, `TEL_OPERATOR`, `IDENTIFY_TYPE`, `IDENTIFY_TIME`, `STATUS`\n" +
            ") \n" +
            "VALUES\n" +
            "  (\n" +
            "    '#{segment},#{province},#{city},#{area},#{teloperator},#{identifytype},#{identifytime},#{status}\n" +
            "  ) ON DUPLICATE KEY UPDATE")
    int insertOrUpdate(PhoneSegment record);

    int insertSelective(PhoneSegment record);

    PhoneSegment selectByPrimaryKey(String segment);

    int updateByPrimaryKeySelective(PhoneSegment record);

    int updateByPrimaryKey(PhoneSegment record);
}