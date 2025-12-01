package com.example.devmachine.config;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author maqidi
 * @version 1.0
 * @create 2025-11-21 19:36
 */
@Configuration
@Slf4j
public class KubernetesConfig {
    //如果是控制多集群，也可以有多个config
    @Value("${kube.config}")
    private String configUrl;
    private final ResourceLoader resourceLoader;

    public KubernetesConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    public KubernetesClient kubernetesClient() throws IOException {
        //读取
        File kubeConfigFile = resourceLoader.getResource(configUrl).getFile();
        Config conf = Config.fromKubeconfig(Files.readString(kubeConfigFile.toPath()));
        conf.setConnectionTimeout(5000);  // 设置连接超时为 1.5 秒
        conf.setRequestTimeout(5000);     // 设置请求超时为 2 秒
        conf.setRequestRetryBackoffLimit(2); // 重试次数
        conf.setNamespace("devmachine-system");

        KubernetesClient kubernetesClient = null;
        try {
            kubernetesClient = new KubernetesClientBuilder().withConfig(conf).build();
        } catch (Exception e) {
            log.info("init kubernetes client failed, ", e);
            throw new RuntimeException(e);
        }
        return kubernetesClient;
    }
}
