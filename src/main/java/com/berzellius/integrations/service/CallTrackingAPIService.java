package com.berzellius.integrations.service;

import com.berzellius.integrations.basic.exception.APIAuthException;
import com.berzellius.integrations.calltrackingru.dto.api.calltracking.Call;
import com.berzellius.integrations.calltrackingru.dto.api.calltracking.CallTrackingSourceCondition;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseErrorHandler;

import java.util.Date;
import java.util.List;

/**
 * Created by berz on 04.09.2016.
 */
@Service
public interface CallTrackingAPIService {
    void setProjects(Integer[] projects);

    List<CallTrackingSourceCondition> getAllMarketingChannelsFromCalltracking() throws APIAuthException;


    /**
     * Загрузка звонков
     * @param from - с какого времени
     * @param to - по какое
     * @param startIndex - начальное смещение
     * @param maxResults - максимальное количество звонков за один запрос
     * @param project - идентификатор проекта
     * @return
     * @throws APIAuthException
     */
    List<Call> getCalls(Date from, Date to, Long startIndex, Integer maxResults, Integer project) throws APIAuthException;

    @Deprecated
    List<Call> getCalls(Date from, Date to, Long startIndex, Integer maxResults) throws APIAuthException;

    @Deprecated
    List<Call> getCalls(Date from, Date to, Long startIndex) throws APIAuthException;

    String getLogin();

    void setLogin(String login);

    String getPassword();

    void setPassword(String password);

    String getLoginURL();

    void setLoginURL(String loginURL);

    HttpMethod getLoginMethod();

    void setLoginMethod(HttpMethod loginMethod);

    String getApiURL();

    void setApiURL(String apiURL);

    HttpMethod getApiMethod();

    void setApiMethod(HttpMethod apiMethod);

    void setErrorHandler(ResponseErrorHandler errorHandler);

    String getWebSiteLogin();

    void setWebSiteLogin(String webSiteLogin);

    String getWebSitePassword();

    void setWebSitePassword(String webSitePassword);

    String getWebSiteLoginUrl();

    void setWebSiteLoginUrl(String webSiteLoginUrl);

    String getWebSiteLoginMethod();

    void setWebSiteLoginMethod(String webSiteLoginMethod);

    Integer getProjectIdBySite(String origin);
}
