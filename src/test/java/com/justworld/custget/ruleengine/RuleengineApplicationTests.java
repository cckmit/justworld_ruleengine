package com.justworld.custget.ruleengine;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Base64Utils;

import java.io.UnsupportedEncodingException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RuleengineApplicationTests {

	@Test
	public void contextLoads() {
		try {
			System.out.println(Base64Utils.encodeToString("1".getBytes("ISO8859-1")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

}
