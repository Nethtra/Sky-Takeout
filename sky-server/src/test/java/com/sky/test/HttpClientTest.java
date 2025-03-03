package com.sky.test;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * 测试HttpClient发起http请求   需要先启动服务 再运行测试才能成功发请求
 * 实际操作已经封装到工具类中
 * @author 王天一
 * @version 1.0
 */
@SpringBootTest
public class HttpClientTest {
    /**
     * 测试get请求
     *
     * @throws IOException
     */
    @Test
    public void testGet() throws IOException {
        //创建httpClient对象   注意HttpClient只是个接口  CloseableHttpClient是实现类
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //创建请求对象   因为拦截器还没有拦user  所以不需要jwt就能直接发请求
        HttpGet httpGet = new HttpGet("http://localhost:8080/user/shop/status");
        //execute发起请求
        CloseableHttpResponse response = httpClient.execute(httpGet);//获取响应结果

        //解析响应结果
        int statusCode = response.getStatusLine().getStatusCode();//获取响应状态码
        System.out.println("响应状态码：" + statusCode);
        HttpEntity entity = response.getEntity();//获取响应体
        String body = EntityUtils.toString(entity);//使用EntityUtils转成字符串
        System.out.println("响应体：" + body);
        //释放资源
        response.close();
        httpClient.close();
    }

    /**
     * 测试post请求
     */
    @Test
    public void testPost() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost:8080/admin/employee/login");

        JSONObject jsonObject = new JSONObject();//3用fastJson转
        jsonObject.put("username", "admin");
        jsonObject.put("password", "123456");
        StringEntity entity = new StringEntity(jsonObject.toString());//2HttpEntity是一个接口 new实现类  构造器需要String  而且要自己转成json格式
        entity.setContentEncoding("utf-8");//4设置编码方式
        entity.setContentType("application/json");//4设置请求内容格式
        httpPost.setEntity(entity);//1设置请求体 需要一个HttpEntity的参数


        CloseableHttpResponse response = httpClient.execute(httpPost);
        int statusCode = response.getStatusLine().getStatusCode();
        System.out.println("响应状态码：" + statusCode);
        HttpEntity responseBody = response.getEntity();
        String body = EntityUtils.toString(responseBody);//使用EntityUtils转成字符串
        System.out.println("响应体：" + body);

        response.close();
        httpClient.close();
    }
}
