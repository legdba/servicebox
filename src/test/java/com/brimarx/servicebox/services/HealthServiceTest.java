package com.brimarx.servicebox.services;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by vincent on 16/08/15.
 */
public class HealthServiceTest {
    private HealthService srv = new HealthService();

    @Test
    public void testCheck()
    {
        Assert.assertEquals(srv.check(), "up");
    }

    @Test
    public void testCheckOrFail()
    {
        double ratio=0.5;
        double sum=0;
        int iter=10000;
        for (int i = iter; i > 0; i--) {
            try {
                srv.checkOrFail(ratio); // 50% chance of success
                sum+=1;
            } catch (RuntimeException e) {
                // ignore
            }
        }
        double mean = sum / iter;
        // Check mean with +/-5%
        Assert.assertTrue( mean < ratio*1.05 && mean > ratio*0.95, "failed with mean="+mean);
    }
}
