package com.brimarx.servicebox;

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
    /*
    @Test
    public void testDelayedEcho() throws InterruptedException
    {
        long t0 = System.currentTimeMillis();
        Assert.assertEquals(srv.delayedEcho("hello"), "hello");
        long t1 = System.currentTimeMillis();
        Assert.assertTrue(t1-t0 > 1900);
    }

    @Test
    public void testExpensiveEcho() throws InterruptedException
    {
        Assert.assertEquals(srv.expensiveEcho("hello"), "hello");
    }*/
}
