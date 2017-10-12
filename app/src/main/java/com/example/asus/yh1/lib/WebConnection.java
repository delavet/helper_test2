package com.example.asus.yh1.lib;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by asus on 2017/10/12 0012.
 */
public class WebConnection {
    public static Parameter connectWithPost(String url,ArrayList<Parameter> params){
        if (params == null || params.size() == 0) {
            return connectWithGet(url);
        }
        try {
            url = url.trim();
            HttpParams httpParams = new BasicHttpParams();

            DefaultHttpClient httpClient = HTTPSClient.getHttpClient(httpParams);
            HttpPost httpPost = new HttpPost(url);


            List<NameValuePair> pList = new ArrayList<NameValuePair>();
            if (params != null) {
                for (Parameter paraItem : params) {
                    String string = paraItem.value;
                    if (string == null || "".equals(string)) continue;
                    pList.add(new BasicNameValuePair(paraItem.name, paraItem.value));
                }
            }
            Cookies.addCookie(httpPost);
            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");
            if (pList.size() != 0)
                httpPost.setEntity(new UrlEncodedFormEntity(pList, "utf-8"));

            HttpResponse httpResponse = httpClient.execute(httpPost);

            int returncode = httpResponse.getStatusLine().getStatusCode();
            Cookies.setCookie(httpResponse, url);
            String value = "";
            if (returncode == 200) {
                BufferedReader bf;
                bf = new BufferedReader(
                        new InputStreamReader(httpResponse.getEntity().getContent()));


                String line = bf.readLine();
                while (line != null) {
                    value += line + "\n";
                    line = bf.readLine();
                }
                value = value.trim();
            }
            Parameter parameters = new Parameter(returncode+"", value);
            return parameters;
        } catch (Exception e) {
            e.printStackTrace();
            return new Parameter("-1", "");
        }
    }
    public static Parameter connectWithGet(String url) {
        try {
            url = url.trim();
            HttpParams httpParams = new BasicHttpParams();

            DefaultHttpClient httpClient = HTTPSClient.getHttpClient(httpParams);
            HttpGet httpGet = new HttpGet(url);
            Cookies.addCookie(httpGet);
            HttpResponse httpResponse = httpClient.execute(httpGet);


            int returncode = httpResponse.getStatusLine().getStatusCode();

            Cookies.setCookie(httpResponse, url);
            String value = "";
            if (returncode == 200) {
                BufferedReader bf;
                    bf = new BufferedReader(
                            new InputStreamReader(httpResponse.getEntity().getContent()));


                String line = bf.readLine();
                while (line != null) {
                    value += line + "\n";
                    line = bf.readLine();
                }
            }
            return new Parameter(returncode+"", value);
        } catch (Exception e) {
            return new Parameter("-1", "");
        }
    }
}
