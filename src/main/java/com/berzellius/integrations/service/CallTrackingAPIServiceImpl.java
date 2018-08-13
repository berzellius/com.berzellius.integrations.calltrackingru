package com.berzellius.integrations.service;

import com.berzellius.integrations.basic.exception.APIAuthException;
import com.berzellius.integrations.basic.service.APIServiceRequestsImpl;
import com.berzellius.integrations.calltrackingru.dto.api.calltracking.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by berz on 22.09.2015.
 */
@Service
public class CallTrackingAPIServiceImpl extends APIServiceRequestsImpl implements CallTrackingAPIService {

    private String auth;
    private Integer[] projects = {};

    private String login;
    private String password;
    private String loginURL;
    private HttpMethod loginMethod;
    private String webSiteLogin;
    private String webSitePassword;
    private String webSiteLoginUrl;
    private String webSiteLoginMethod;

    private String apiURL;
    private HttpMethod apiMethod;



    private String metrics = "ct:duration,ct:answer_time";
    private String dimensions = "ct:caller,ct:datetime,ct:source,ct:status";

    private ResponseErrorHandler errorHandler;

    private List<String> cookies;


    private static final Logger log = Logger.getLogger("dd");


    public Integer[] getProjects() {
        return projects;
    }

    private MultiValueMap<String, String> createWebSiteLoginParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("email", this.getWebSiteLogin());
        params.add("password", this.getWebSitePassword());

        return params;
    }

    private MultiValueMap<String, String> createLoginParams(){
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("account_type", "calltracking");
        params.add("login", this.getLogin());
        params.add("password", this.getPassword());
        params.add("service","analytics");

        return params;
    }

    private HttpEntity<MultiValueMap<String, String>> requestByParams(MultiValueMap<String, String> params){
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Content-type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        if(this.cookies != null) {
            requestHeaders.add("Cookie", String.join(";", this.cookies));
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, requestHeaders);
        return  request;
    }

    @Override
    public void setProjects(Integer[] projects) {
        this.projects = projects;
    }

    public RestTemplate getRestTemplate(){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(this.errorHandler);

        MappingJackson2HttpMessageConverter jsonHttpMessageConverter = new MappingJackson2HttpMessageConverter();
        FormHttpMessageConverter httpMessageConverter = new FormHttpMessageConverter();

        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.TEXT_HTML);
        mediaTypes.add(MediaType.APPLICATION_JSON);

        List<MediaType> formsMediaTypes = new ArrayList<MediaType>();
        formsMediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);

        List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();

        jsonHttpMessageConverter.setSupportedMediaTypes(mediaTypes);
        httpMessageConverter.setSupportedMediaTypes(formsMediaTypes);

        messageConverters.add(jsonHttpMessageConverter);
        messageConverters.add(httpMessageConverter);
        restTemplate.setMessageConverters(messageConverters);

        return restTemplate;
    }

    @Override
    public void logIn() throws APIAuthException {

        HttpEntity<MultiValueMap<String, String>> request = this
                .requestByParams(this.createLoginParams());

        CallTrackingAuth callTrackingAuth = (CallTrackingAuth) this
                .request(
                        this.getLoginURL(), this.getLoginMethod(), request, CallTrackingAuth.class
                );


        if(
                !(callTrackingAuth.getError_code().equals(0) &&
                        callTrackingAuth.getStatus().equals("ok"))
                ){
            throw new APIAuthException("Cant authentificate!");
        }

        this.auth = callTrackingAuth.getData();

        this.reLogins = 0;
    }

    private boolean checkWebsiteLoggedIn(){
        if(this.cookies == null)
            return false;

        for(String cookie : this.cookies){
            //System.out.println("found cookie:" + cookie);
            String[] strings = cookie.split("=");
            if(
                    strings.length > 0 &&
                        (
                                strings[0].equals("I-CMS_AUTH") ||
                                        strings[0].equals("laravel_session")
                        )
                    )
            {
                return true;
            }
        }

        return false;
    }


    @Override
    public List<CallTrackingSourceCondition> getAllMarketingChannelsFromCalltracking() throws APIAuthException {
        websiteLogIn();
        log.info("logged in successfully..");
        List<CallTrackingSourceCondition> callTrackingSourceConditions = new LinkedList<>();

        for(Integer projectId : this.getProjects()){
            log.info("project " + projectId);
            getAllMarketingChannelsFromCalltrackingByProjectId(projectId, callTrackingSourceConditions);
        }

        return callTrackingSourceConditions;
    }

    private void getAllMarketingChannelsFromCalltrackingByProjectId(Integer projectId, List<CallTrackingSourceCondition> listToPush) throws APIAuthException {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("sidx", "id");
        params.add("sord", "desc");

        HttpEntity<MultiValueMap<String, String>> request = this
                .requestByParams(params);

        CallTrackingWebsiteSources callTrackingWebsiteSources = this
                .request("https://calltracking.ru/admin/" + projectId + "/sources_manage", HttpMethod.POST, request, CallTrackingWebsiteSources.class);

        for(CallTrackingWebsiteSource callTrackingWebsiteSource : callTrackingWebsiteSources.getRows()){
            MultiValueMap<String, String> params1 = new LinkedMultiValueMap<>();
            params1.add("id", callTrackingWebsiteSource.getSourceId().toString());
            params1.add("action", "get_source_info");

            HttpEntity<MultiValueMap<String, String>> request1 = this
                    .requestByParams(params1);

            CallTrackingWebsiteSourceConditionsResponse callTrackingWebsiteSourceConditionsResponse = this
                    .request("https://calltracking.ru/admin/" + projectId + "/sources_manage/view/actions", HttpMethod.POST, request1, CallTrackingWebsiteSourceConditionsResponse.class);

            if(callTrackingWebsiteSourceConditionsResponse.getResponse() == null || callTrackingWebsiteSourceConditionsResponse.getResponse().getConditions() == null){
                return;
            }

            for(CallTrackingWebsiteSourceCondition callTrackingWebsiteSourceCondition : callTrackingWebsiteSourceConditionsResponse.getResponse().getConditions()) {
                String[] utmSources = callTrackingWebsiteSourceCondition.getUtm_source().split(",");
                String[] utmMediums = callTrackingWebsiteSourceCondition.getUtm_medium().split(",");
                String[] utmCampaigns = callTrackingWebsiteSourceCondition.getUtm_campaign().split(",");

                for(String utmSource : utmSources){
                    for(String utmMedium : utmMediums){
                        for(String utmCampaign : utmCampaigns){
                            int truth = 0;
                            if(!utmSource.equals(""))
                                truth++;
                            if(!utmMedium.equals(""))
                                truth++;
                            if(!utmCampaign.equals(""))
                                truth++;

                            CallTrackingSourceCondition callTrackingSourceCondition = new CallTrackingSourceCondition();
                            callTrackingSourceCondition.setSourceName(callTrackingWebsiteSource.getSourceName());
                            callTrackingSourceCondition.setUtmSource(utmSource);
                            callTrackingSourceCondition.setUtmMedium(utmMedium);
                            callTrackingSourceCondition.setUtmCampaign(utmCampaign);
                            callTrackingSourceCondition.setSourceId(callTrackingWebsiteSourceCondition.getSourceId());
                            callTrackingSourceCondition.setProjectId(projectId);
                            callTrackingSourceCondition.setTruth(truth);
                            callTrackingSourceCondition.setPhonesCount(callTrackingWebsiteSource.getPhones_count());


                            listToPush.add(callTrackingSourceCondition);
                        }
                    }
                }
            }

        }
    }

    private void websiteLogIn() throws APIAuthException {
        this.reLogins = 0;
        System.out.println("relogins: " + this.reLogins);

        while (!this.checkWebsiteLoggedIn()) {
            System.out.println("loggin in..");
            if(this.reLogins > this.reLoginsMax){
                throw new APIAuthException("cant login to calltracking website");
            }
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.add("Content-type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("login", this.getWebSiteLogin());
            params.add("password", this.getWebSitePassword());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, requestHeaders);
            this.cookies = new LinkedList<>();
            System.out.println("url: " + this.getWebSiteLoginUrl());
            HttpEntity<String> response = restTemplate.exchange(this.getWebSiteLoginUrl(), HttpMethod.POST, request, String.class);

            HttpHeaders httpHeaders = response.getHeaders();
            if(httpHeaders.containsKey("Set-Cookie")){
                for(String cookie : httpHeaders.get("Set-Cookie")){
                    this.cookies.add(cookie.split(";")[0]);
                }
            }

            this.reLogins++;
        }
    }


    private MultiValueMap<String, String> apiParams(Date from, Date to, Long startIndex, Integer maxResults){
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("dimensions", this.dimensions);
        params.add("metrics", this.metrics);

        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
        params.add("start-date", dt.format(from));
        params.add("end-date", dt.format(to));
        params.add("sort", "ct:datetime");

        if(startIndex != null){
            params.add("start-index", startIndex.toString());
        }

        if(maxResults != null){
            params.add("max-results", maxResults.toString());
        }

        return params;
    }

    @Override
    public List<Call> getCalls(Date from, Date to, Long startIndex, Integer maxResults, Integer project) throws APIAuthException {
        Assert.notNull(from);
        Assert.notNull(to);
        Assert.isTrue(from.before(to));
        Assert.notNull(startIndex);
        Assert.isTrue(startIndex >= 0l);
        Assert.notNull(maxResults);
        Assert.isTrue(maxResults > 0);
        Assert.notNull(project);

        List<Call> calls = new LinkedList<>();

        this.addCalls(from, to, startIndex, maxResults, project, calls);
        return calls;
    }

    private void addCalls(Date from, Date to, Long startIndex, Integer maxResults, Integer project, List<Call> calls) throws APIAuthException {
        MultiValueMap<String, String> params = apiParams(from, to, startIndex, maxResults);
        params.add("project", project.toString());

        CallTrackingData callTrackingData = this.getData(params);
        for(Call call : callTrackingData.getData()){
            call.setState(Call.State.NEW);
            call.setProjectId(project);
            calls.add(call);
        }
    }

    @Override
    public List<Call> getCalls(Date from, Date to, Long startIndex, Integer maxResults) throws APIAuthException {
        List<Call> calls = new LinkedList<>();

        for(Integer project : this.projects){
            this.addCalls(from, to, startIndex, maxResults, project, calls);
        }
        return calls;
    }

    @Override
    public List<Call> getCalls(Date from, Date to, Long startIndex) throws APIAuthException {
        return getCalls(from, to, startIndex, null);
    }

    private CallTrackingData getData(MultiValueMap<String, String> params) throws APIAuthException {
        if(this.auth == null){
            this.logIn();
        }

        params.add("auth", this.auth);

        HttpEntity<MultiValueMap<String, String>> req = this
                .requestByParams(params);

        CallTrackingData callTrackingData = (CallTrackingData) this
                .request(
                        this.getApiURL(), this.getApiMethod(), req, CallTrackingData.class
                );

        if(callTrackingData.getError_code().equals(4)){
            this.reLogin();
            return this.getData(params);
        }
        else{
            this.reLogins = 0;

            if(
                    !(callTrackingData.getError_code().equals(0) &&
                            callTrackingData.getStatus().equals("ok"))
                    ){
                throw new IllegalStateException("Error: " + callTrackingData.getError_code() + "( " + callTrackingData.getError_text() + " )");
            }

            return callTrackingData;
        }
    }

    @Override
    public String getLogin() {
        return login;
    }

    @Override
    public void setLogin(String login) {
        this.login = login;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getLoginURL() {
        return loginURL;
    }

    @Override
    public void setLoginURL(String loginURL) {
        this.loginURL = loginURL;
    }

    @Override
    public HttpMethod getLoginMethod() {
        return loginMethod;
    }

    @Override
    public void setLoginMethod(HttpMethod loginMethod) {
        this.loginMethod = loginMethod;
    }

    @Override
    public String getApiURL() {
        return apiURL;
    }

    @Override
    public void setApiURL(String apiURL) {
        this.apiURL = apiURL;
    }

    @Override
    public HttpMethod getApiMethod() {
        return apiMethod;
    }

    @Override
    public void setApiMethod(HttpMethod apiMethod) {
        this.apiMethod = apiMethod;
    }

    @Override
    public void setErrorHandler(ResponseErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public String getWebSiteLogin() {
        return webSiteLogin;
    }

    @Override
    public void setWebSiteLogin(String webSiteLogin) {
        this.webSiteLogin = webSiteLogin;
    }

    @Override
    public String getWebSitePassword() {
        return webSitePassword;
    }

    @Override
    public void setWebSitePassword(String webSitePassword) {
        this.webSitePassword = webSitePassword;
    }

    @Override
    public String getWebSiteLoginUrl() {
        return webSiteLoginUrl;
    }

    @Override
    public void setWebSiteLoginUrl(String webSiteLoginUrl) {
        this.webSiteLoginUrl = webSiteLoginUrl;
    }

    @Override
    public String getWebSiteLoginMethod() {
        return webSiteLoginMethod;
    }

    @Override
    public void setWebSiteLoginMethod(String webSiteLoginMethod) {
        this.webSiteLoginMethod = webSiteLoginMethod;
    }

    @Override
    public Integer getProjectIdBySite(String origin) {
        return projects[0];
    }
}
