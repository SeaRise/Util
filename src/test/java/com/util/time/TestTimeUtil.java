package com.util.time;

import org.junit.Assert;
import org.junit.Test;

public class TestTimeUtil {
	
	@Test
	public void testCurrentTimeMillis() throws InterruptedException {
		long l1 = TimeUtil.currentTimeMillis();
		Assert.assertEquals(l1, TimeUtil.currentTimeMillis());
		Thread.sleep(Long.parseLong(System.getProperty("notify.systimer.tick", "50"))+1);
		Assert.assertNotEquals(l1, TimeUtil.currentTimeMillis());
	}
}
