package pers.laineyc.blackbox.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.net.http.HttpClient;

@Slf4j
@Component
public class HttpClientService {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public HttpClient getHttpClient() {
        return httpClient;
    }

}
