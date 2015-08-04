package com.brimarx.servicebox.services;

import com.brimarx.servicebox.services.EchoService;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by vincebou on 31/10/2014.
 */
public class EchoServiceTest
{
    private EchoService srv = new EchoService();

    @Test
    public void testEcho()
    {
        Assert.assertEquals(srv.echo("hello"), "hello");
    }

    @Test
    public void testDelayedEcho() throws Exception
    {
        Assert.assertEquals(srv.delayedEcho("hello", 1), "hello");
    }

    @Test
    public void expensiveEcho()
    {
        Assert.assertEquals(srv.expensiveEcho("hello"), "hello");
    }
}
