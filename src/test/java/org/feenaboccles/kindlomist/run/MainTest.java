package org.feenaboccles.kindlomist.run;

import static org.junit.Assert.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class MainTest {

	@Test
	public void testOnGoodInput() {
		String[] input = StringUtils.split("--username budge -p password -d 2033-12-30");
		Main m = new Main();
		m.call(input);
		
		assertEquals("budge", m.getUsername());
		assertEquals("password", m.getPassword());
		assertEquals("2033-12-30", m.getDateStamp());
	}

}
