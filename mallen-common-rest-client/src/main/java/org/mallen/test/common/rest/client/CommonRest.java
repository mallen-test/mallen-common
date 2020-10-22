package org.mallen.test.common.rest.client;

import com.fasterxml.jackson.core.type.TypeReference;
import org.mallen.test.common.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.File;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用于访问非spring cloud环境的微服务的基类，将get、post方法封装，子类只需要传递url、参数以及responseType即可获取到结果。
 * <h2>配置步骤</h2>
 * <ul>
 * <li>
 * 1、使用{@link org.springframework.context.annotation.Import Import}注解引入{@link CommonRestConfiguration}配
 * 置类。{@link CommonRestConfiguration}会配置两个默认的restTemplate和asyncRestTemplate。如果不希望使用这两个默认的
 * bean，可以不引入{@link CommonRestConfiguration}，但需要手动定义这两个bean。
 * </li>
 * <li>
 * 2、若需要使用{@link #buildSign(String, Map, PathVariable)}进行URL签名时，需使用{@link EnableConfigurationProperties}
 * 开启{@link MallenSecurityProperties}配置引入并在配置文件设置mallen.security.appkey和mallen.security.secretKey两个配置项的值。
 * </li>
 * <li>
 * 3、若需要配置{@code RestTemplate}或{@code AsyncRestTemplate}的{@code setConnectTimeout}和{@code setReadTimeout}参数时，
 * 可以在配置文件设置{@code mallen.rest.connectTimeout}和{@code mallen.rest.readTimeout}两个配置项的值，默认为5分钟。
 * </li>
 * </ul>
 * <h2>使用示例</h2>
 * 继承CommonRest，子类可以使用父类中的所有方法访问网络。网络访问支持的方式和事例如下：
 * <ul>
 * <li>GET方式
 * <pre class="code">
 * public void testGet() {
 *    String aid = "10";
 *    Map<String, Object> params = new HashMap<>();
 *    params.put("adminId", aid);
 *    QueryAdminResponse response = baseRest.get(domain, getAdminById, params, new ResponseType<Response<QueryAdmin>>(){});
 *    System.out.println("响应数据:"+JSONUtil.writeValueAsString(response));
 * }</pre>
 * <li>POST JSON使用对象
 * <pre class="code">
 * public void testPostEntry() {
 *    VinReq  vinReq   = new VinReq();
 *    vinReq.getVins().add("LMGFE1G88D1000001");
 *    List<String> vins = new ArrayList<>();
 *    vins.add("LMGFE1G88D1000001");
 *    String response = baseRest.postEntity(testDomain, getListVehicleAllInfoByVinList, vins, new ResponseType<String>(){});
 *    System.out.println("响应数据:"+JSONUtil.writeValueAsString(response));
 * }</pre>
 * <li>POST JSON使用map
 * <pre class="code">
 * public void testPostEntry2() {
 *    Map<String, Object> params = new HashMap<>();
 *    params.put("vin", "LMGFE1G88D1000001");
 *    params.put("ssid", "ssdfsdf");
 *    params.put("wifiPwd", "sdfsfsdf");
 *    params.put("pwdEnCodeType", "1");
 *    params.put("wifiStatus", "ON");
 *    String response = baseRest.postEntity(testDomain, setVehicleConfig, params, new ResponseType<String>(){});
 *    System.out.println("响应数据:"+JSONUtil.writeValueAsString(response));
 * }</pre>
 * <li>POST FORM
 * <pre class="code">
 * public void testPostForm() {
 *    Map<String, String> params = new HashMap<>();
 *    params.put("username", "233343434");
 *    params.put("password", "123abc!!!");
 *    params.put("clientId", RandomStringUtils.randomAlphabetic(16));
 *    params.put("clientType", "BROWSER");
 *    String response = baseRest.postForm(domain, loginByUserName, params, new ResponseType<String>(){});
 *    System.out.println("响应数据:"+JSONUtil.writeValueAsString(response));
 * }</pre>
 * <li>上传文件
 * <pre class="code">
 * public void testFileUpload() {
 *    Map<String, Object> params = new HashMap<>();
 *    File  f  =new File("mallen/jmc-b-tsp-sp/demo/file/a.txt");
 *    params.put("file",f);
 *    params.put("path","/tempErrorExcel/");
 *    params.put("description","测试文件上传");
 *    String response = baseRest.postForm(domain, fileserverUploadUri, params, new ResponseType<String>(){});
 *    System.out.println("响应数据:"+JSONUtil.writeValueAsString(response));
 * }</pre>
 * <li>path参数
 * <pre class="code">
 * public void pathByMap() {
 *    PathVariable pathVariable = new PathVariable().add("vin", "vin123");
 *    Response resp = commonRest.get(url, pathVariable, new ResponseType<Response>() {});
 * }</pre>
 * <li> parseResponse提供篡改响应输出的功能，高级用户可以在其中抛出异常，获取http 状态码等高级功能，注意：请勿篡改结构，如果连结构一起篡改，会导致反向序列化失败。
 * <pre class="code">
 * {@literal @}Override
 * protected String parseResponse(ResponseEntity<String> responseEntity) {
 *    if(responseEntity.getStatusCode().is2xxSuccessful()){
 *    System.out.println("调用成功");
 *    return  responseEntity.getBody();
 *    }
 *    else  if (responseEntity.getStatusCode().is5xxServerError()){
 *    throw  new RuntimeException("调用错误");
 *    }else {
 *    System.out.println("未知错误");
 *    }
 *    return null;
 *    }</pre>
 * <li> appendHeader添加额外的header 适用于所有请求都需要添加的情况，
 * <pre class="code">
 * {@literal @}Override
 * protected Map<String, Object> appendHeader() {
 *    Map<String,Object>  header = new HashMap<>();
 *    header.put("a","version1");
 *    header.put("token","143434");
 *    return header ;
 * }</pre>
 * * <li>path catchError  捕获异常，建议调用第三方客户端时实现。
 * <pre class="code">
 * {@literal @}Override
 * protected void catchError(int code, String message,Exception e ) {
 *  System.out.println("http Code : " +code);
 *  System.out.println("http message : " +message);
 *  throw  new   RuntimeException(e);
 * }</pre>
 *
 * @author tao.wang
 * @date 10/16/18
 */
public class CommonRest {
    private static final Logger logger = LoggerFactory.getLogger(CommonRest.class);
    /**
     * 匹配以https或者http开头的字符串，直到下一个/结束。比如http://127.0.0.1/veh-status/abc，该正则匹配到的是http://127.0.0.1/
     */
    private static final Pattern HTTP_URL_PATTERN = Pattern.compile("^(http://|https://)[^/]*/");
    private static final Pattern PATH_VARIBLE_PATTERN = Pattern.compile("\\{([^}]+?)\\}");
    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";

    @Resource
    private RestTemplate restTemplate;

    @Autowired(required = false)
    @Qualifier("loadBalancedRestTemplate")
    private RestTemplate loadBalancedRestTemplate;
    @Autowired(required = false)
    private CommonRestProperties restProperties;

    /**
     * 提交表单
     *
     * @param url          全路径
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String url, Object formObject, ResponseType<T> responseType) {
        return exchangePostForm(url, null, null, formObject, null, responseType);
    }

    /**
     * 提交表单
     *
     * @param url          全路径
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String url, HeaderMap header, Object formObject, ResponseType<T> responseType) {
        return exchangePostForm(url, null, null, formObject, header, responseType);
    }

    /**
     * 提交表单
     *
     * @param url          全路径
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String url, Object formObject, Class<T> responseType) {
        return exchangePostForm(url, null, null, formObject, null, responseType(responseType));
    }

    /**
     * 提交表单
     *
     * @param url          全路径
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String url, HeaderMap header, Object formObject, Class<T> responseType) {
        return exchangePostForm(url, null, null, formObject, header, responseType(responseType));
    }

    /**
     * 提交表单
     *
     * @param url          全路径
     * @param queryParams  query参数
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String url, Map<String, Object> queryParams, Object formObject, ResponseType<T> responseType) {
        return exchangePostForm(url, queryParams, null, formObject, null, responseType);
    }

    /**
     * 提交表单
     *
     * @param url          全路径
     * @param queryParams  query参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String url, Map<String, Object> queryParams, HeaderMap header, Object formObject, ResponseType<T> responseType) {
        return exchangePostForm(url, queryParams, null, formObject, header, responseType);
    }

    /**
     * 提交表单
     *
     * @param url          全路径
     * @param queryParams  query参数
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String url, Map<String, Object> queryParams, Object formObject, Class<T> responseType) {
        return exchangePostForm(url, queryParams, null, formObject, null, responseType(responseType));
    }

    /**
     * 提交表单
     *
     * @param url          全路径
     * @param queryParams  query参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String url, Map<String, Object> queryParams, HeaderMap header, Object formObject, Class<T> responseType) {
        return exchangePostForm(url, queryParams, null, formObject, header, responseType(responseType));
    }

    /**
     * 提交表单
     *
     * @param url          全路径
     * @param pathVariable path参数
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String url, PathVariable pathVariable, Object formObject, ResponseType<T> responseType) {
        return exchangePostForm(url, null, pathVariable, formObject, null, responseType);
    }

    /**
     * 提交表单
     *
     * @param url          全路径
     * @param pathVariable path参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String url, PathVariable pathVariable, HeaderMap header, Object formObject, ResponseType<T> responseType) {
        return exchangePostForm(url, null, pathVariable, formObject, header, responseType);
    }

    /**
     * 提交表单
     *
     * @param url          全路径
     * @param pathVariable path参数
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String url, PathVariable pathVariable, Object formObject, Class<T> responseType) {
        return exchangePostForm(url, null, pathVariable, formObject, null, responseType(responseType));
    }

    /**
     * 提交表单
     *
     * @param url          全路径
     * @param pathVariable path参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String url, PathVariable pathVariable, HeaderMap header, Object formObject, Class<T> responseType) {
        return exchangePostForm(url, null, pathVariable, formObject, header, responseType(responseType));
    }

    /**
     * 提交表单
     *
     * @param url          全路径
     * @param pathVariable path参数
     * @param queryParams  query参数
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String url, PathVariable pathVariable, Map<String, Object> queryParams, Object formObject, ResponseType<T> responseType) {
        return exchangePostForm(url, queryParams, pathVariable, formObject, null, responseType);
    }

    /**
     * 提交表单
     *
     * @param url          全路径
     * @param pathVariable path参数
     * @param queryParams  query参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String url, PathVariable pathVariable, Map<String, Object> queryParams, HeaderMap header, Object formObject, ResponseType<T> responseType) {
        return exchangePostForm(url, queryParams, pathVariable, formObject, header, responseType);
    }

    /**
     * 提交表单
     *
     * @param url          全路径
     * @param pathVariable path参数
     * @param queryParams  query参数
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String url, PathVariable pathVariable, Map<String, Object> queryParams, Object formObject, Class<T> responseType) {
        return exchangePostForm(url, queryParams, pathVariable, formObject, null, responseType(responseType));
    }

    /**
     * 提交表单
     *
     * @param url          全路径
     * @param pathVariable path参数
     * @param queryParams  query参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String url, PathVariable pathVariable, Map<String, Object> queryParams, HeaderMap header, Object formObject, Class<T> responseType) {
        return exchangePostForm(url, queryParams, pathVariable, formObject, header, responseType(responseType));
    }

    /**
     * 提交表单
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String domain, String url, Object formObject, ResponseType<T> responseType) {
        return exchangePostForm(domain + url, null, null, formObject, null, responseType);
    }

    /**
     * 提交表单
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String domain, String url, HeaderMap header, Object formObject, ResponseType<T> responseType) {
        return exchangePostForm(domain + url, null, null, formObject, header, responseType);
    }

    /**
     * 提交表单
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String domain, String url, Object formObject, Class<T> responseType) {
        return exchangePostForm(domain + url, null, null, formObject, null, responseType(responseType));
    }

    /**
     * 提交表单
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String domain, String url, HeaderMap header, Object formObject, Class<T> responseType) {
        return exchangePostForm(domain + url, null, null, formObject, header, responseType(responseType));
    }

    /**
     * 提交表单
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param queryParams  query参数
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String domain, String url, Map<String, Object> queryParams, Object formObject, ResponseType<T> responseType) {
        return exchangePostForm(domain + url, queryParams, null, formObject, null, responseType);
    }

    /**
     * 提交表单
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param queryParams  query参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String domain, String url, Map<String, Object> queryParams, HeaderMap header, Object formObject, ResponseType<T> responseType) {
        return exchangePostForm(domain + url, queryParams, null, formObject, header, responseType);
    }

    /**
     * 提交表单
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param queryParams  query参数
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String domain, String url, Map<String, Object> queryParams, Object formObject, Class<T> responseType) {
        return exchangePostForm(domain + url, queryParams, null, formObject, null, responseType(responseType));
    }

    /**
     * 提交表单
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param queryParams  query参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String domain, String url, Map<String, Object> queryParams, HeaderMap header, Object formObject, Class<T> responseType) {
        return exchangePostForm(domain + url, queryParams, null, formObject, header, responseType(responseType));
    }

    /**
     * 提交表单
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param pathVariable path参数
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String domain, String url, PathVariable pathVariable, Object formObject, ResponseType<T> responseType) {
        return exchangePostForm(domain + url, null, pathVariable, formObject, null, responseType);
    }

    /**
     * 提交表单
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param pathVariable path参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String domain, String url, PathVariable pathVariable, HeaderMap header, Object formObject, ResponseType<T> responseType) {
        return exchangePostForm(domain + url, null, pathVariable, formObject, header, responseType);
    }

    /**
     * 提交表单
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param pathVariable path参数
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String domain, String url, PathVariable pathVariable, Object formObject, Class<T> responseType) {
        return exchangePostForm(domain + url, null, pathVariable, formObject, null, responseType(responseType));
    }

    /**
     * 提交表单
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param pathVariable path参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String domain, String url, PathVariable pathVariable, HeaderMap header, Object formObject, Class<T> responseType) {
        return exchangePostForm(domain + url, null, pathVariable, formObject, header, responseType(responseType));
    }

    /**
     * 提交表单
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param pathVariable path参数
     * @param queryParams  query参数
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String domain, String url, PathVariable pathVariable, Map<String, Object> queryParams,
                          Object formObject, ResponseType<T> responseType) {
        return exchangePostForm(domain + url, queryParams, pathVariable, formObject, null, responseType);
    }

    /**
     * 提交表单
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param pathVariable path参数
     * @param queryParams  query参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String domain, String url, PathVariable pathVariable, Map<String, Object> queryParams,
                          HeaderMap header, Object formObject, ResponseType<T> responseType) {
        return exchangePostForm(domain + url, queryParams, pathVariable, formObject, header, responseType);
    }

    /**
     * 提交表单
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param pathVariable path参数
     * @param queryParams  query参数
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String domain, String url, PathVariable pathVariable, Map<String, Object> queryParams,
                          Object formObject, Class<T> responseType) {
        return exchangePostForm(domain + url, queryParams, pathVariable, formObject, null, responseType(responseType));
    }

    /**
     * 提交表单
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param pathVariable path参数
     * @param queryParams  query参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param formObject   表单参数，支持 map 和 简单对象
     * @param responseType 期望响应的对象
     */
    public <T> T postForm(String domain, String url, PathVariable pathVariable, Map<String, Object> queryParams,
                          HeaderMap header, Object formObject, Class<T> responseType) {
        return exchangePostForm(domain + url, queryParams, pathVariable, formObject, header, responseType(responseType));
    }

    /**
     * 提交json 数据
     *
     * @param url          全路径
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String url, Object body, ResponseType<T> responseType) {
        return exchangePostEntity(url, null, null, null, body, responseType);
    }

    /**
     * 提交json 数据
     *
     * @param url          全路径
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String url, HeaderMap header, Object body, ResponseType<T> responseType) {
        return exchangePostEntity(url, null, null, header, body, responseType);
    }

    /**
     * 提交json 数据
     *
     * @param url          全路径
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String url, Object body, Class<T> responseType) {
        return exchangePostEntity(url, null, null, null, body, responseType(responseType));
    }

    /**
     * 提交json 数据
     *
     * @param url          全路径
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String url, HeaderMap header, Object body, Class<T> responseType) {
        return exchangePostEntity(url, null, null, header, body, responseType(responseType));
    }

    /**
     * 提交json 数据
     *
     * @param url          全路径
     * @param queryParams  query参数
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String url, Map<String, Object> queryParams, Object body, ResponseType<T> responseType) {
        return exchangePostEntity(url, queryParams, null, null, body, responseType);
    }

    /**
     * 提交json 数据
     *
     * @param url          全路径
     * @param queryParams  query参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String url, Map<String, Object> queryParams, HeaderMap header, Object body, ResponseType<T> responseType) {
        return exchangePostEntity(url, queryParams, null, header, body, responseType);
    }

    /**
     * 提交json 数据
     *
     * @param url          全路径
     * @param queryParams  query参数
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String url, Map<String, Object> queryParams, Object body, Class<T> responseType) {
        return exchangePostEntity(url, queryParams, null, null, body, responseType(responseType));
    }

    /**
     * 提交json 数据
     *
     * @param url          全路径
     * @param queryParams  query参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String url, Map<String, Object> queryParams, HeaderMap header, Object body, Class<T> responseType) {
        return exchangePostEntity(url, queryParams, null, header, body, responseType(responseType));
    }

    /**
     * 提交json 数据
     *
     * @param url          全路径
     * @param pathVariable path参数
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String url, PathVariable pathVariable, Object body, ResponseType<T> responseType) {
        return exchangePostEntity(url, null, pathVariable, null, body, responseType);
    }

    /**
     * 提交json 数据
     *
     * @param url          全路径
     * @param pathVariable path参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String url, PathVariable pathVariable, HeaderMap header, Object body, ResponseType<T> responseType) {
        return exchangePostEntity(url, null, pathVariable, header, body, responseType);
    }

    /**
     * 提交json 数据
     *
     * @param url          全路径
     * @param pathVariable path参数
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String url, PathVariable pathVariable, Object body, Class<T> responseType) {
        return exchangePostEntity(url, null, pathVariable, null, body, responseType(responseType));
    }

    /**
     * 提交json 数据
     *
     * @param url          全路径
     * @param pathVariable path参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String url, PathVariable pathVariable, HeaderMap header, Object body, Class<T> responseType) {
        return exchangePostEntity(url, null, pathVariable, header, body, responseType(responseType));
    }

    /**
     * 提交json 数据
     *
     * @param url          全路径
     * @param pathVariable path参数
     * @param queryParams  query参数
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String url, PathVariable pathVariable, Map<String, Object> queryParams, Object body, ResponseType<T> responseType) {
        return exchangePostEntity(url, queryParams, pathVariable, null, body, responseType);
    }

    /**
     * 提交json 数据
     *
     * @param url          全路径
     * @param pathVariable path参数
     * @param queryParams  query参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String url, PathVariable pathVariable, Map<String, Object> queryParams, HeaderMap header, Object body, ResponseType<T> responseType) {
        return exchangePostEntity(url, queryParams, pathVariable, header, body, responseType);
    }

    /**
     * 提交json 数据
     *
     * @param url          全路径
     * @param pathVariable path参数
     * @param queryParams  query参数
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String url, PathVariable pathVariable, Map<String, Object> queryParams, Object body, Class<T> responseType) {
        return exchangePostEntity(url, queryParams, pathVariable, null, body, responseType(responseType));
    }

    /**
     * 提交json 数据
     *
     * @param url          全路径
     * @param pathVariable path参数
     * @param queryParams  query参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String url, PathVariable pathVariable, Map<String, Object> queryParams, HeaderMap header, Object body, Class<T> responseType) {
        return exchangePostEntity(url, queryParams, pathVariable, header, body, responseType(responseType));
    }

    /**
     * 提交json 数据
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String domain, String url, Object body, ResponseType<T> responseType) {
        return exchangePostEntity(domain + url, null, null, null, body, responseType);
    }

    /**
     * 提交json 数据
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String domain, String url, HeaderMap header, Object body, ResponseType<T> responseType) {
        return exchangePostEntity(domain + url, null, null, header, body, responseType);
    }

    /**
     * 提交json 数据
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String domain, String url, Object body, Class<T> responseType) {
        return exchangePostEntity(domain + url, null, null, null, body, responseType(responseType));
    }

    /**
     * 提交json 数据
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String domain, String url, HeaderMap header, Object body, Class<T> responseType) {
        return exchangePostEntity(domain + url, null, null, header, body, responseType(responseType));
    }

    /**
     * 提交json 数据
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param queryParams  query参数
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String domain, String url, Map<String, Object> queryParams, Object body, ResponseType<T> responseType) {
        return exchangePostEntity(domain + url, queryParams, null, null, body, responseType);
    }

    /**
     * 提交json 数据
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param queryParams  query参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String domain, String url, Map<String, Object> queryParams, HeaderMap header, Object body, ResponseType<T> responseType) {
        return exchangePostEntity(domain + url, queryParams, null, header, body, responseType);
    }

    /**
     * 提交json 数据
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param queryParams  query参数
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String domain, String url, Map<String, Object> queryParams, Object body, Class<T> responseType) {
        return exchangePostEntity(domain + url, queryParams, null, null, body, responseType(responseType));
    }

    /**
     * 提交json 数据
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param queryParams  query参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String domain, String url, Map<String, Object> queryParams, HeaderMap header, Object body, Class<T> responseType) {
        return exchangePostEntity(domain + url, queryParams, null, header, body, responseType(responseType));
    }

    /**
     * 提交json 数据
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param pathVariable path参数
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String domain, String url, PathVariable pathVariable, Object body, ResponseType<T> responseType) {
        return exchangePostEntity(domain + url, null, pathVariable, null, body, responseType);
    }

    /**
     * 提交json 数据
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param pathVariable path参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String domain, String url, PathVariable pathVariable, HeaderMap header, Object body, ResponseType<T> responseType) {
        return exchangePostEntity(domain + url, null, pathVariable, header, body, responseType);
    }

    /**
     * 提交json 数据
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param pathVariable path参数
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String domain, String url, PathVariable pathVariable, Object body, Class<T> responseType) {
        return exchangePostEntity(domain + url, null, pathVariable, null, body, responseType(responseType));
    }

    /**
     * 提交json 数据
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param pathVariable path参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String domain, String url, PathVariable pathVariable, HeaderMap header, Object body, Class<T> responseType) {
        return exchangePostEntity(domain + url, null, pathVariable, header, body, responseType(responseType));
    }

    /**
     * 提交json 数据
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param pathVariable path参数
     * @param queryParams  query参数
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String domain, String url, PathVariable pathVariable, Map<String, Object> queryParams, Object body, ResponseType<T> responseType) {
        return exchangePostEntity(domain + url, queryParams, pathVariable, null, body, responseType);
    }

    /**
     * 提交json 数据
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param pathVariable path参数
     * @param queryParams  query参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String domain, String url, PathVariable pathVariable, Map<String, Object> queryParams, HeaderMap header, Object body, ResponseType<T> responseType) {
        return exchangePostEntity(domain + url, queryParams, pathVariable, header, body, responseType);
    }

    /**
     * 提交json 数据
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param pathVariable path参数
     * @param queryParams  query参数
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String domain, String url, PathVariable pathVariable, Map<String, Object> queryParams, Object body, Class<T> responseType) {
        return exchangePostEntity(domain + url, queryParams, pathVariable, null, body, responseType(responseType));
    }

    /**
     * 提交json 数据
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param pathVariable path参数
     * @param queryParams  query参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param body         body
     * @param responseType 期望响应的对象
     */
    public <T> T postEntity(String domain, String url, PathVariable pathVariable, Map<String, Object> queryParams, HeaderMap header, Object body, Class<T> responseType) {
        return exchangePostEntity(domain + url, queryParams, pathVariable, header, body, responseType(responseType));
    }

    /**
     * GET 请求
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param responseType 期望响应的对象
     */
    public <T> T get(String domain, String url, ResponseType<T> responseType) {
        return exchangeGet(domain + url, null, null, null, responseType);

    }

    /**
     * GET 请求
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param responseType 期望响应的对象
     */
    public <T> T get(String domain, String url, HeaderMap header, ResponseType<T> responseType) {
        return exchangeGet(domain + url, null, null, header, responseType);

    }

    /**
     * GET 请求
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param responseType 期望响应的对象
     */
    public <T> T get(String domain, String url, Class<T> responseType) {
        return exchangeGet(domain + url, null, null, null, responseType(responseType));

    }

    /**
     * GET 请求
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param responseType 期望响应的对象
     */
    public <T> T get(String domain, String url, HeaderMap header, Class<T> responseType) {
        return exchangeGet(domain + url, null, null, header, responseType(responseType));

    }

    /**
     * GET 请求
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param queryParams  query参数
     * @param responseType 期望响应的对象
     */
    public <T> T get(String domain, String url, Map<String, Object> queryParams, ResponseType<T> responseType) {
        return exchangeGet(domain + url, queryParams, null, null, responseType);
    }

    /**
     * GET 请求
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param queryParams  query参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param responseType 期望响应的对象
     */
    public <T> T get(String domain, String url, Map<String, Object> queryParams, HeaderMap header, ResponseType<T> responseType) {
        return exchangeGet(domain + url, queryParams, null, header, responseType);
    }

    /**
     * GET 请求
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param queryParams  query参数
     * @param responseType 期望响应的对象
     */
    public <T> T get(String domain, String url, Map<String, Object> queryParams, Class<T> responseType) {
        return exchangeGet(domain + url, queryParams, null, null, responseType(responseType));
    }

    /**
     * GET 请求
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param queryParams  query参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param responseType 期望响应的对象
     */
    public <T> T get(String domain, String url, Map<String, Object> queryParams, HeaderMap header, Class<T> responseType) {
        return exchangeGet(domain + url, queryParams, null, header, responseType(responseType));
    }

    /**
     * GET 请求
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param pathVariable path参数
     * @param responseType 期望响应的对象
     */
    public <T> T get(String domain, String url, PathVariable pathVariable, ResponseType<T> responseType) {
        return exchangeGet(domain + url, null, pathVariable, null, responseType);
    }

    /**
     * GET 请求
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param pathVariable path参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param responseType 期望响应的对象
     */
    public <T> T get(String domain, String url, PathVariable pathVariable, HeaderMap header, ResponseType<T> responseType) {
        return exchangeGet(domain + url, null, pathVariable, header, responseType);
    }

    /**
     * GET 请求
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param pathVariable path参数
     * @param responseType 期望响应的对象
     */
    public <T> T get(String domain, String url, PathVariable pathVariable, Class<T> responseType) {
        return exchangeGet(domain + url, null, pathVariable, null, responseType(responseType));
    }

    /**
     * GET 请求
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param pathVariable path参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param responseType 期望响应的对象
     */
    public <T> T get(String domain, String url, PathVariable pathVariable, HeaderMap header, Class<T> responseType) {
        return exchangeGet(domain + url, null, pathVariable, header, responseType(responseType));
    }

    /**
     * GET 请求
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param pathVariable path参数
     * @param queryParams  query参数
     * @param responseType 期望响应的对象
     */
    public <T> T get(String domain, String url, PathVariable pathVariable, Map<String, Object> queryParams, ResponseType<T> responseType) {
        return exchangeGet(domain + url, queryParams, pathVariable, null, responseType);
    }

    /**
     * GET 请求
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param pathVariable path参数
     * @param queryParams  query参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param responseType 期望响应的对象
     */
    public <T> T get(String domain, String url, PathVariable pathVariable, Map<String, Object> queryParams, HeaderMap header, ResponseType<T> responseType) {
        return exchangeGet(domain + url, queryParams, pathVariable, header, responseType);
    }

    /**
     * GET 请求
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param pathVariable path参数
     * @param queryParams  query参数
     * @param responseType 期望响应的对象
     */
    public <T> T get(String domain, String url, PathVariable pathVariable, Map<String, Object> queryParams, Class<T> responseType) {
        return exchangeGet(domain + url, queryParams, pathVariable, null, responseType(responseType));
    }

    /**
     * GET 请求
     *
     * @param domain       域名
     * @param url          uri 定位符
     * @param pathVariable path参数
     * @param queryParams  query参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param responseType 期望响应的对象
     */
    public <T> T get(String domain, String url, PathVariable pathVariable, Map<String, Object> queryParams, HeaderMap header, Class<T> responseType) {
        return exchangeGet(domain + url, queryParams, pathVariable, header, responseType(responseType));
    }

    /**
     * GET 请求
     *
     * @param url          全路径
     * @param responseType 期望响应的对象
     */
    public <T> T get(String url, ResponseType<T> responseType) {
        return exchangeGet(url, null, null, null, responseType);
    }

    /**
     * GET 请求
     *
     * @param url          全路径
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param responseType 期望响应的对象
     */
    public <T> T get(String url, HeaderMap header, ResponseType<T> responseType) {
        return exchangeGet(url, null, null, header, responseType);
    }

    /**
     * GET 请求
     *
     * @param url          全路径
     * @param responseType 期望响应的对象
     */
    public <T> T get(String url, Class<T> responseType) {
        return exchangeGet(url, null, null, null, responseType(responseType));
    }

    /**
     * GET 请求
     *
     * @param url          全路径
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param responseType 期望响应的对象
     */
    public <T> T get(String url, HeaderMap header, Class<T> responseType) {
        return exchangeGet(url, null, null, header, responseType(responseType));
    }

    /**
     * GET 请求
     *
     * @param url          全路径
     * @param queryParams  query参数
     * @param responseType 期望响应的对象
     */
    public <T> T get(String url, Map<String, Object> queryParams, ResponseType<T> responseType) {
        return exchangeGet(url, queryParams, null, null, responseType);
    }

    /**
     * GET 请求
     *
     * @param url          全路径
     * @param queryParams  query参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param responseType 期望响应的对象
     */
    public <T> T get(String url, Map<String, Object> queryParams, HeaderMap header, ResponseType<T> responseType) {
        return exchangeGet(url, queryParams, null, header, responseType);
    }

    /**
     * GET 请求
     *
     * @param url          全路径
     * @param queryParams  query参数
     * @param responseType 期望响应的对象
     */
    public <T> T get(String url, Map<String, Object> queryParams, Class<T> responseType) {
        return exchangeGet(url, queryParams, null, null, responseType(responseType));
    }

    /**
     * GET 请求
     *
     * @param url          全路径
     * @param queryParams  query参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param responseType 期望响应的对象
     */
    public <T> T get(String url, Map<String, Object> queryParams, HeaderMap header, Class<T> responseType) {
        return exchangeGet(url, queryParams, null, header, responseType(responseType));
    }

    /**
     * GET 请求
     *
     * @param url          全路径
     * @param pathVariable path参数
     * @param responseType 期望响应的对象
     */
    public <T> T get(String url, PathVariable pathVariable, ResponseType<T> responseType) {
        return exchangeGet(url, null, pathVariable, null, responseType);
    }

    /**
     * GET 请求
     *
     * @param url          全路径
     * @param pathVariable path参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param responseType 期望响应的对象
     */
    public <T> T get(String url, PathVariable pathVariable, HeaderMap header, ResponseType<T> responseType) {
        return exchangeGet(url, null, pathVariable, header, responseType);
    }

    /**
     * GET 请求
     *
     * @param url          全路径
     * @param pathVariable path参数
     * @param responseType 期望响应的对象
     */
    public <T> T get(String url, PathVariable pathVariable, Class<T> responseType) {
        return exchangeGet(url, null, pathVariable, null, responseType(responseType));
    }

    /**
     * GET 请求
     *
     * @param url          全路径
     * @param pathVariable path参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param responseType 期望响应的对象
     */
    public <T> T get(String url, PathVariable pathVariable, HeaderMap header, Class<T> responseType) {
        return exchangeGet(url, null, pathVariable, header, responseType(responseType));
    }


    /**
     * GET 请求
     *
     * @param url          全路径
     * @param pathVariable path参数
     * @param queryParams  query参数
     * @param responseType 期望响应的对象
     */
    public <T> T get(String url, PathVariable pathVariable, Map<String, Object> queryParams, ResponseType<T> responseType) {
        return exchangeGet(url, queryParams, pathVariable, null, responseType);
    }

    /**
     * GET 请求
     *
     * @param url          全路径
     * @param pathVariable path参数
     * @param queryParams  query参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param responseType 期望响应的对象
     */
    public <T> T get(String url, PathVariable pathVariable, Map<String, Object> queryParams, HeaderMap header, ResponseType<T> responseType) {
        return exchangeGet(url, queryParams, pathVariable, header, responseType);
    }

    /**
     * GET 请求
     *
     * @param url          全路径
     * @param pathVariable path参数
     * @param queryParams  query参数
     * @param responseType 期望响应的对象
     */
    public <T> T get(String url, PathVariable pathVariable, Map<String, Object> queryParams, Class<T> responseType) {
        return exchangeGet(url, queryParams, pathVariable, null, responseType(responseType));
    }

    /**
     * GET 请求
     *
     * @param url          全路径
     * @param pathVariable path参数
     * @param queryParams  query参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param responseType 期望响应的对象
     */
    public <T> T get(String url, PathVariable pathVariable, Map<String, Object> queryParams, HeaderMap header, Class<T> responseType) {
        return exchangeGet(url, queryParams, pathVariable, header, responseType(responseType));
    }

    ////////////////////////////////////////////////////////
    ////////////////////////不可重写部分//////////////////////

    /**
     * Get 请求
     *
     * @param url          url
     * @param queryParams  查询参数
     * @param pathVariable path参数
     * @param headerMap    自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param responseType 期望响应的对象
     * @param <T>          期望响应的对象类型
     * @return 返回期望响应的对象
     */
    private <T> T exchangeGet(String url, Map<String, Object> queryParams, PathVariable pathVariable,
                              HeaderMap headerMap, ResponseType<T> responseType) {
        if (queryParams == null) {
            queryParams = new HashMap<>(3);
        }
        url = buildUrl(url, queryParams, pathVariable);
        // 添加header
        HttpHeaders headers = new HttpHeaders();
        appendCustomerHeader(headers, headerMap);
        addAccept(headers);
        // 合并query参数和path参数
        Map<String, Object> uriVariable = mergeQueryAndPathParams(queryParams, pathVariable);
        // 发起请求
        return exchange(url, HttpMethod.GET, new HttpEntity<>(headers), uriVariable, responseType);
    }

    /**
     * POST 请求
     *
     * @param url          url
     * @param queryParams  query参数
     * @param pathVariable path参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param body         body
     * @param responseType 期望响应的对象
     * @param header       header参数
     * @param <T>          期望响应的对象类型
     * @return 返回期望响应的对象
     */
    private <T> T exchangePostEntity(String url, Map<String, Object> queryParams, PathVariable pathVariable,
                                     HeaderMap header, Object body, ResponseType<T> responseType) {
        if (queryParams == null) {
            queryParams = new HashMap<>(3);
        }
        url = buildUrl(url, queryParams, pathVariable);
        HttpEntity httpEntity = buildBodyEntry(body, header);
        // 合并query参数和path参数
        Map<String, Object> uriVariable = mergeQueryAndPathParams(queryParams, pathVariable);
        return exchange(url, HttpMethod.POST, httpEntity, uriVariable, responseType);
    }

    /**
     * POST 表单提交
     *
     * @param url          url
     * @param queryParams  query参数
     * @param formObject   表单参数
     * @param header       自定义header，如果是公用header建议覆盖{@link CommonRest#appendHeader()}方法
     * @param responseType 期望响应的对象
     * @param <T>
     * @return
     */
    private <T> T exchangePostForm(String url, Map<String, Object> queryParams,
                                   PathVariable pathVariable, Object formObject, HeaderMap header, ResponseType<T> responseType) {
        if (queryParams == null) {
            queryParams = new HashMap<>(3);
        }
        url = buildUrl(url, queryParams, pathVariable);
        HttpEntity httpEntity = buildFormEntry(formObject, header);
        // 合并query参数和path参数
        Map<String, Object> uriVariable = mergeQueryAndPathParams(queryParams, pathVariable);
        // 发起请求
        return exchange(url, HttpMethod.POST, httpEntity, uriVariable, responseType);
    }

    /**
     * 构建url，将query参数和签名参数拼接到url
     *
     * @param url
     * @param queryParams  query参数，在计算签名后，会在其中包含appkey、sign、signt参数
     * @param pathVariable
     * @return
     */
    private String buildUrl(String url, Map<String, Object> queryParams, PathVariable pathVariable) {
        // 过滤queryParams中没有传值的参数
        queryParams.entrySet().removeIf(stringObjectEntry -> StringUtils.isEmpty(stringObjectEntry.getValue()));
        // 拼接url参数，参数格式如：url?a={a}&b={b}，其中a,b为queryParams中的key
        StringBuilder urlBuilder = new StringBuilder(url).append("?");
        Map<String, Object> treeMap = new TreeMap<>(queryParams);
        for (Map.Entry<String, Object> entry : treeMap.entrySet()) {
            String key = entry.getKey();
            urlBuilder.append(key).append("={").append(key).append("}&");
        }
        url = urlBuilder.replace(urlBuilder.length() - 1, urlBuilder.length(), "").toString();
        return url;
    }

    /**
     * 合并查询参数和path参数
     *
     * @param queryParams  查询参数
     * @param pathVariable path参数
     * @return 返回合并后的参数集合
     */
    private Map<String, Object> mergeQueryAndPathParams(Map<String, Object> queryParams, PathVariable pathVariable) {
        if (pathVariable == null || pathVariable.getParams() == null || pathVariable.getParams().keySet().size() == 0) {
            return queryParams;
        }
        for (Object key : pathVariable.getParams().keySet()) {
            queryParams.put(key.toString(), pathVariable.getParams().get(key));
        }
        return queryParams;
    }

    /**
     * 构建 formObject   对应的 httpEntry
     *
     * @param formObject 表单参数，支持map 和 简单对象， map 中可以放入文件， 简单对象中不支持 多层对象和文件
     * @return 返回一个HTTP请求实体内容
     */
    private HttpEntity buildFormEntry(Object formObject, HeaderMap headerMap) {
        // 构建file part
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        // 组合所有part
        Map<String, Object> formMap;
        if (formObject instanceof Map) {
            //noinspection unchecked
            formMap = (Map<String, Object>) formObject;
        } else {
            //noinspection unchecked
            formMap = JsonUtil.readValue(JsonUtil.writeValueAsString(formObject), Map.class);
        }
        // 构造form参数
        for (Map.Entry<String, Object> entry : formMap.entrySet()) {
            // 上传文件的时候需要覆盖
            if (entry.getValue() instanceof File) {
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                File file = (File) entry.getValue();
                FileSystemResource resource = new FileSystemResource(file);
                multiValueMap.add(entry.getKey(), resource);
            } else {
                multiValueMap.add(entry.getKey(), entry.getValue());
            }
        }
        // 添加headers
        appendCustomerHeader(headers, headerMap);
        // 添加Accept Header
        addAccept(headers);
        return new HttpEntity<>(multiValueMap, headers);
    }

    /**
     * 追加子类自定义的Header参数
     *
     * @param headers   HTTP请求header参数
     * @param headerMap 子类自定义header参数
     */
    private void appendCustomerHeader(HttpHeaders headers, HeaderMap headerMap) {
        Map<String, Object> extraHeader = appendHeader();
        if (extraHeader != null && !extraHeader.isEmpty()) {
            for (Map.Entry<String, Object> entry : extraHeader.entrySet()) {
                if (entry.getValue() != null && !StringUtils.isEmpty(String.valueOf(entry.getValue()))) {
                    headers.add(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
        }
        if (headerMap != null && headerMap.isNotEmpty()) {
            headerMap.keySet().forEach(key -> {
                headers.add(key.toString(), headerMap.get(key).toString());
            });
        }
    }

    /**
     * 构建 body    对应的 httpEntry
     *
     * @param body      body 对象
     * @param headerMap 扩展header参数
     * @return HttpEntity
     */
    private HttpEntity buildBodyEntry(Object body, HeaderMap headerMap) {
        if (body != null) {
            HttpHeaders headers = new HttpHeaders();
            appendCustomerHeader(headers, headerMap);
            addContentType(headers);
            addAccept(headers);
            return new HttpEntity<>(body, headers);
        }

        return null;
    }

    /**
     * 执行一次HTTP请求
     *
     * @param url          请求地址
     * @param method       请求方式
     * @param httpEntity   请求内容
     * @param uriVariable  URI
     * @param responseType 请求响应类型
     * @param <T>          期望的响应类型
     * @return 期望类型的响应结果
     */
    private <T> T exchange(String url, HttpMethod method, HttpEntity httpEntity, Map<String, Object> uriVariable, ResponseType<T> responseType) {
        ResponseEntity<T> responseEntity = null;
        Throwable throwable = null;
        long beginTime = System.currentTimeMillis();
        try {
            RestTemplate template = getTemplate(url);
            // 开始访问
            ParameterizedTypeReference<T> type = new ParameterizedTypeReference<T>() {
                @Override
                public Type getType() {
                    return responseType.getType();
                }
            };
            responseEntity = template.exchange(url, method, httpEntity, type, uriVariable);
            return parseResponse(responseType, responseEntity);
        } catch (Exception e) {
            throwable = e;
            if (e instanceof HttpStatusCodeException) {
                catchError(((HttpStatusCodeException) e).getStatusCode().value(), ((HttpStatusCodeException) e).getStatusText(), (HttpStatusCodeException) e);
            }
            throw e;
        } finally {
            printLog(url, method, httpEntity, uriVariable, responseEntity, throwable, beginTime);
        }
    }

    /**
     * 为子类提供拦截response的入口
     *
     * @param responseType
     * @param responseEntity
     * @param <T>
     * @return
     */
    private <T> T parseResponse(ResponseType responseType, ResponseEntity<T> responseEntity) {
        if (!responseEntity.hasBody()) {
            return null;
        }
        // 将body转换为json字符串
        T body = responseEntity.getBody();
        String stringBody;
        if (!(body instanceof String)) {
            stringBody = JsonUtil.writeValueAsString(body);
        } else {
            stringBody = (String) body;
        }
        ResponseEntity<String> stringResp = new ResponseEntity<>(stringBody, responseEntity.getHeaders(), responseEntity.getStatusCode());
        String cusResp = parseResponse(stringResp);
        // 如果未覆盖response，则返回原始数据
        if (StringUtils.isEmpty(cusResp)) {
            return body;
        }
        return JsonUtil.readValue(cusResp, new TypeReference<T>() {
            @Override
            public Type getType() {
                return responseType.getType();
            }
        });
    }

    /**
     * 添加内容类型
     *
     * @param headers header参数
     */
    private void addContentType(HttpHeaders headers) {
        String contentType = contentType();
        if (contentType != null) {
            headers.add(HttpHeaders.CONTENT_TYPE, contentType);
        }
    }

    private void addAccept(HttpHeaders headers) {
        String accept = accept();
        if (accept != null) {
            headers.add(HttpHeaders.ACCEPT, accept);
        }
    }

    ////////////////////////////////////////////////////////
    /////////////////////////可重写部分///////////////////////
    /**
     * 获取URI地址
     *
     * @param url 请求完整地址
     * @return 请求URI地址
     */
    protected String getUri(String url) {
        // 如果url中包含http开头，则只取uri部分，比如url为：httt://localhost/veh-status/query，则只取veh-status/query
        String uri = url;
        if (url.startsWith(HTTP) || url.startsWith(HTTPS)) {
            String[] part = HTTP_URL_PATTERN.split(url);
            if (part.length == 2) {
                uri = part[1];
            } else {
                uri = "";
            }
        } else if (url.startsWith("/")) {
            uri = url.substring(url.indexOf("/") + 1);
        }
        // 去除编写配置文件引起的空格问题
        uri = uri.replaceAll(" ", "");
        return uri;
    }

    /**
     * 替换uri中的path变量
     *
     * @param url
     * @param pathVariable
     */
    protected String replacePathVariable(String url, PathVariable pathVariable) {
        if (pathVariable == null) {
            return url;
        }
        return replaceUriVariable(url, pathVariable.getParams());
    }

    /**
     * 替换uri中的标签变量
     *
     * @param url
     * @param params
     */
    protected String replaceUriVariable(String url, Map<String, Object> params) {
        if (params == null || params.keySet().size() == 0) {
            return url;
        }
        Matcher matcher = PATH_VARIBLE_PATTERN.matcher(url);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = params.get(key) == null ? "" : params.get(key).toString();
            String replacement = Matcher.quoteReplacement(value);
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 适配 普通的 http 请求 和 走 eureka 的请求，以 . 为判定标志，未来可能不准，现在
     *
     * @param url
     * @return
     */
    protected RestTemplate getTemplate(String url) {
        // 截取域名
        int index = url.indexOf("://");
        if (index == -1) {
            return restTemplate;
        }
        String domain = url.substring(index + 3);
        index = domain.indexOf("/");
        if (-1 != index) {
            domain = domain.substring(0, index);
        }
        if (domain.indexOf(".") != -1 || domain.indexOf("localhost") != -1) {
            return restTemplate;
        }
        return loadBalancedRestTemplate;
        // add By Mallen 上方直接通过截取字符串并判断域名中是否有点号来区分是否使用了loadBalance，如果有特别需求，必须精确判断，可以使用下方代码实现
//        if (null == discoveryClient) {
//            return restTemplate;
//        }
//        // 截取域名
//        int index = url.indexOf("://");
//        if (index == -1) {
//            return restTemplate;
//        }
//        String domain = url.substring(index + 3);
//        index = domain.indexOf("/");
//        if (-1 != index) {
//            domain = url.substring(0, index);
//        }
//        // 查看域名是否为服务名
//        List<ServiceInstance> instances = discoveryClient.getInstances(domain);
//        if (null == instances || instances.size() == 0) {
//            return restTemplate;
//        } else {
//            return loadBalancedRestTemplate;
//        }
    }

    protected Boolean useLoadBalance(String url) {
        int index = url.indexOf("://");
        if (index == -1) {
            return false;
        }
        String domain = url.substring(index + 3);
        index = domain.indexOf("/");
        if (-1 != index) {
            domain = domain.substring(0, index);
        }
        if (domain.indexOf(".") != -1 || domain.indexOf("localhost") != -1) {
            return false;
        }
        return true;
        // add By Mallen 上方直接通过截取字符串并判断域名中是否有点号来区分是否使用了loadBalance，如果有特别需求，必须精确判断，可以使用下方代码实现
//        if (null == discoveryClient) {
//            return false;
//        }
//        // 截取域名
//        // 截取域名
//        int index = url.indexOf("://");
//        if (index == -1) {
//            return false;
//        }
//        String domain = url.substring(index + 3);
//        index = domain.indexOf("/");
//        if (-1 != index) {
//            domain = url.substring(0, index);
//        }
//        List<ServiceInstance> instances = discoveryClient.getInstances(domain);
//        if (null == instances || instances.size() == 0) {
//            return false;
//        } else {
//            return true;
//        }
    }

    /**
     * 提供统一的 响应拦截器，可以替换响应的内容，子类可以覆盖 并且可以替换期望获得的结果类型。
     *
     * @param responseEntity 不管真实的响应数据格式是什么，此处的输入都为json格式的字符串。
     * @return 如果需要覆盖response，请返回json格式的字符串，且该字符串必须能转换为T对象。如果不覆盖response，请返回null。
     */
    protected String parseResponse(ResponseEntity<String> responseEntity) {
        return null;
    }

    /**
     * 提供统一的 header 添加方法，如果需要定义每个接口的header，请使用带有{@link HeaderMap}参数的接口
     *
     * @return
     */
    protected Map<String, Object> appendHeader() {
        return null;
    }

    /**
     * 设置请求的Content-Type，默认不设置，子类可以覆盖该方法来设置请求的Content-Type
     *
     * @return
     */
    protected String contentType() {
        return null;
    }

    /**
     * 设置接收的Content-Type，默认不设置，子类可以覆盖该方法来设置接收的Content-Type
     *
     * @return
     */
    protected String accept() {
        return null;
    }

    /**
     * 提供统一的异常捕获方法，当访问发生http错误时，子类可以自定义怎么处理。默认直接将该异常抛出
     */
    protected void catchError(Integer code, String message, HttpStatusCodeException e) {
        throw new RuntimeException(e);
    }

    /**
     * 打印日志，子类可以覆盖此方法自定义日志打印逻辑
     *
     * @param <T>         响应内容类型
     * @param url         请求地址
     * @param method      请求方式
     * @param entity      请求实体
     * @param uriVariable 请求uri变量
     * @param response    响应体
     * @param throwable   请求错误信息
     * @param beginTime   请求开始时间
     */
    protected <T> void printLog(String url, HttpMethod method, HttpEntity entity, Map<String, Object> uriVariable, ResponseEntity<T> response, Throwable throwable, long beginTime) {
        if (!logger.isDebugEnabled()) {
            return;
        }
        try {
            long endTime = System.currentTimeMillis();
            logger.debug("请求开始时间：{}", formatLogTime(beginTime));
            logger.debug("请求URL: {}", replaceUriVariable(url, uriVariable));
            logger.debug("请求方法类型: {}", method.name());

            if (entity != null) {
                if (entity.getHeaders() != null) {

                    HttpHeaders httpHeaders = entity.getHeaders();
                    Set<String> keySet = entity.getHeaders().keySet();
                    logger.debug("打印请求头开始-------打印请求头开始");
                    for (String key : keySet) {
                        logger.debug("  {}-->{}", key, httpHeaders.getFirst(key));
                    }
                    logger.debug("打印请求头完成-------打印请求头完成");
                }
                if (!(entity.getHeaders() != null && entity.getHeaders().getContentType() != null && entity.getHeaders().getContentType().equals(MediaType.MULTIPART_FORM_DATA))
                        && entity.getBody() != null) {
                    logger.debug("请求body数据: {}", JsonUtil.writeValueAsString(entity.getBody()));
                }
            }
            logger.debug("原始响应信息: {}", response == null ? null : JsonUtil.writeValueAsString(response.getBody()));
            logger.debug("请求结束时间：{}，耗时：{} ms", formatLogTime(endTime), endTime - beginTime);
        } catch (Exception ex) {
            logger.error("打印访问日志失败：", ex);
        }
    }

    /**
     * 格式化时间格式，支持子类覆盖实现
     *
     * @param logTime
     * @return
     */
    protected String formatLogTime(long logTime) {
        if (null == restProperties || null == restProperties.getLogTimeFormatter()) {
            return String.valueOf(logTime);
        }
        return restProperties.getLogTimeFormatter()
                .format(LocalDateTime.ofInstant(Instant.ofEpochMilli(logTime), ZoneId.systemDefault()));
    }

    ////////////////////////////////////////////////////////
    ////////////////////////静态方法部分//////////////////////

    public static <T> ResponseType<T> responseType(Class<T> tClass) {
        return new ResponseType<T>() {
            @Override
            public Type getType() {
                if (tClass == null) {
                    throw new IllegalArgumentException("Internal error: TypeReference constructed without actual type information");
                }
                return tClass;
            }

            @Override
            public int hashCode() {
                return tClass.hashCode();
            }

            @Override
            public String toString() {
                return "ParameterizedTypeReference<" + tClass + ">";
            }
        };
    }
}
