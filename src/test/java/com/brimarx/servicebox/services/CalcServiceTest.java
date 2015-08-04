package com.brimarx.servicebox.services;

import com.brimarx.servicebox.backend.MemoryBackend;
import com.brimarx.servicebox.services.CalcService;
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

    @Test
    public void testSum()
    {
        CalcService.setBackend(new MemoryBackend());
        Assert.assertEquals(srv.sum("1", 1), 1);
        Assert.assertEquals(srv.sum("1", 2), 3);
        Assert.assertEquals(srv.sum("1", 3), 6);
        Assert.assertEquals(srv.sum("2", 1), 1);
        Assert.assertEquals(srv.sum("2", 2), 3);
        Assert.assertEquals(srv.sum("2", 3), 6);
    }
}
