/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.rest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import pl.sportal.common.utilities.JSONArrayBuilder;
import pl.sportal.common.utilities.JSONObjectExt;


@ApplicationPath("/")
public class RESTApplication extends Application {

    public static String APLIKACJA = "s-portal";

    //--------------------------------------------------------------------------
    // Konstruktor
    //--------------------------------------------------------------------------
    public RESTApplication() {
    }

    //--------------------------------------------------------------------------
    // Metody publiczne
    //--------------------------------------------------------------------------
    public static void addToMap(HashMap<String, JSONArrayBuilder> map, String key, String value) {
        if (map != null && key != null && value != null) {
            if (map.get(key) == null) {
                map.put(key, new JSONArrayBuilder());
            }
            map.put(key, map.get(key).add(value));
        }
    }

    /**
     * Validates Google reCAPTCHA V2 or Invisible reCAPTCHA.
     *
     * @param secretKey Secret key (key given for communication between your
     * site and Google)
     * @param response reCAPTCHA response from client side.
     * (g-recaptcha-response)
     * @return true if validation successful, false otherwise.
     */    
    
    public static synchronized boolean isCaptchaValid(String secretKey, String response) {
        try {
            String url = "https://www.google.com/recaptcha/api/siteverify",
                    params = "secret=" + secretKey + "&response=" + response;

            HttpURLConnection http = (HttpURLConnection) new URL(url).openConnection();
            http.setDoOutput(true);
            http.setRequestMethod("POST");
            http.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded; charset=UTF-8");
            OutputStream output = http.getOutputStream();
            output.write(params.getBytes("UTF-8"));
            output.flush();
            output.close();

            InputStream input = http.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));

            StringBuilder builder = new StringBuilder();
            int cp;
            while ((cp = reader.read()) != -1) {
                builder.append((char) cp);
            }
            JSONObjectExt json = new JSONObjectExt(builder.toString());
            System.out.println(json.toJSONString());
            input.close();

            return json.getBooleanSimple("success");
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return false;
    }
    //--------------------------------------------------------------------------
    // Metody prywatne
    //--------------------------------------------------------------------------
}
