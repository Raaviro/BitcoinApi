package com.vi.api;

import com.vi.validation.BitcoinAddressValidator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.*;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class BitcoinApi extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String address = request.getParameter("address");
        String currency = request.getParameter("currency");

        BitcoinAddressValidator validator = new BitcoinAddressValidator();

        PrintWriter out = response.getWriter();

        if(!validator.validateBitcoinAddress(address)){
            out.print("Address is not valid");
        } else {

            URL balanceUrl = new URL("https://chain.so/api/v2/get_address_balance/BTCTEST/" + address);
            String addressInfo = readFromURL(balanceUrl);

            JSONObject addressInfoObject = null;
            try {
                addressInfoObject = new JSONObject(addressInfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            URL currencyUrl = new URL("https://api.coindesk.com/v1/bpi/currentprice/" + currency + ".json");
            String curremcyInfo = readFromURL(currencyUrl);

            JSONObject currencyInfoObject = null;
            try {
                currencyInfoObject = new JSONObject(curremcyInfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");

            JSONObject responseObject = new JSONObject();

            try {
                responseObject.put("address", address);
                JSONArray arr = new JSONArray();
                JSONObject btcBallanceObject = new JSONObject();
                JSONObject currencyObject = new JSONObject();

                JSONObject confirmedBalanceObject = (JSONObject) addressInfoObject.get("data");
                String btcBallance = confirmedBalanceObject.get("confirmed_balance").toString();

                JSONObject bpiObject = (JSONObject) currencyInfoObject.get("bpi");
                JSONObject currentCurrencyInfoObject = (JSONObject) bpiObject.get(currency);
                String currencyBallance = currentCurrencyInfoObject.get("rate").toString();

                btcBallanceObject.put("BTC", Double.parseDouble(String.format(Locale.ENGLISH, "%.2f", Double.parseDouble(btcBallance))));
                currencyObject.put(currency, getCurrencyAmount(currencyBallance, btcBallance));

                arr.put(btcBallanceObject).put(currencyObject);

                responseObject.put("balance", arr);
                responseObject.put("timestamp", getCurrentTime());

            } catch (JSONException e) {
                e.printStackTrace();
            }
            out.print(responseObject.toString());
        }
    }

    protected String readFromURL(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.addRequestProperty("User-Agent", "Mozilla 4.76");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer buffer = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            buffer.append(inputLine);
        }
        in.close();
        connection.disconnect();

        return buffer.toString();
    }

    protected String getCurrentTime() {
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Kiev");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+hh:mm");
        dateFormat.setTimeZone(timeZone);
        String nowAsISO = dateFormat.format(new Date());

        return nowAsISO;
    }

    protected Double getCurrencyAmount(String currencyBallance, String btcBallance) {
        NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
        Number number = null;
        try {
            number = format.parse(currencyBallance);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        double currencyBallanceDouble = number.doubleValue();

        DecimalFormat df = new DecimalFormat("####");
        String currencyAmount = df.format(currencyBallanceDouble * Double.parseDouble(btcBallance));

        return Double.parseDouble(currencyAmount);
    }
}

