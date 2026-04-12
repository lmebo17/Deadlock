package com.deadlock.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
public class DockerConfig {

    @Bean
    public DockerClient dockerClient(@Value("${app.docker.host}") String dockerHost) {
        var config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();
        var httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(URI.create(dockerHost))
                .build();
        return DockerClientImpl.getInstance(config, httpClient);
    }
}
