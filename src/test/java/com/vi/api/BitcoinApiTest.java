package com.vi.api;

import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static com.jcabi.matchers.RegexMatchers.matchesPattern;
import static org.junit.Assert.*;

public class BitcoinApiTest {
    private static final double DELTA = 1e-15;

    BitcoinApi bitcoinApi = new BitcoinApi();

    @Test
    public void readFromURLTest() {
        URL url = null;
        try {
            url = new URL("https://chain.so/api/v2/get_address_balance/BTCTEST/ms7sJb2SdGQQvWct4rdLmaSfGZtb5CZQuP");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            assertNotNull(bitcoinApi.readFromURL(url));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getCurrencyAmountTest() {
        assertEquals((double)bitcoinApi.getCurrencyAmount("338,478.4314", "15.08672341"), 5106530, DELTA);
    }

    @Test
    public void getCurrentTimeTest() {
        assertThat(bitcoinApi.getCurrentTime(), matchesPattern("\\d{4}-\\d{2}-\\d{2}T(\\d{2}:){2}\\d{2}\\+\\d{2}:\\d{2}"));
    }

}