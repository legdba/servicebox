package com.brimarx.servicebox;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by vincent on 04/08/15.
 */
public class CalcServiceTest {
    private CalcService srv = new CalcService();

    @Test
    public void testAdd()
    {
        Assert.assertEquals(srv.add(3, 4), 7);
    }
}
