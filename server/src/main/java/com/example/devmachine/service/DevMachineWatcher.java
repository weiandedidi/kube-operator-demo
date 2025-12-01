package com.example.devmachine.service;

import com.example.devmachine.model.crd.DevMachine;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.io.IOException;

/**
 * Watch DevMachine status changes
 */
@Service
public class DevMachineWatcher {

    private static final Logger log = LoggerFactory.getLogger(DevMachineWatcher.class);

    @Autowired
    private KubernetesClient client;

    private Closeable watch;

    /**
     * 本地运行的程序的话使用的k8s的config  因为rancher无法访问目前的的集群，没有watcher权限，所以使用这个 rancher的yaml不能watch这个资源的状态
     * Fabric8 客户端 → 访问 Rancher 代理 → Rancher 判断 “你的 SA 是否能访问该集群”
     * → 被 Rancher 拦住 → 403 → Websocket Watch 建立失败
     * 注释掉这里面的watch
     */
//    @PostConstruct
    public void startWatching() {
        log.info("Starting DevMachine watcher...");

        MixedOperation<DevMachine, io.fabric8.kubernetes.api.model.KubernetesResourceList<DevMachine>, Resource<DevMachine>> devMachineClient
                = client.resources(DevMachine.class);

        watch = devMachineClient
                .inAnyNamespace()
                .watch(new Watcher<DevMachine>() {
                    @Override
                    public void eventReceived(Action action, DevMachine resource) {
                        String name = resource.getMetadata().getName();
                        String namespace = resource.getMetadata().getNamespace();

                        log.info("DevMachine {} event: {}/{}", action, namespace, name);

                        if (resource.getStatus() != null) {
                            String phase = resource.getStatus().getPhase();
                            String message = resource.getStatus().getMessage();

                            log.info("DevMachine {}/{} status: phase={}, message={}",
                                    namespace, name, phase, message);

                            // 这里可以添加业务逻辑，例如：
                            // - 当 phase 变为 Running 时发送通知
                            // - 当 phase 变为 Failed 时触发告警
                            // - 更新数据库状态
                            handleStatusChange(namespace, name, phase, message);
                        }
                    }

                    @Override
                    public void onClose(WatcherException cause) {
                        if (cause != null) {
                            log.error("Watcher closed with exception", cause);
                        } else {
                            log.info("Watcher closed");
                        }
                    }
                });

        log.info("DevMachine watcher started");
    }

    private void handleStatusChange(String namespace, String name, String phase, String message) {
        switch (phase) {
            case "Pending":
                log.info("DevMachine {}/{} is pending", namespace, name);
                break;
            case "Creating":
                log.info("DevMachine {}/{} is being created", namespace, name);
                break;
            case "Running":
                log.info("DevMachine {}/{} is now running", namespace, name);
                // 可以在这里发送通知给用户
                break;
            case "Stopping":
                log.info("DevMachine {}/{} is stopping", namespace, name);
                break;
            case "Stopped":
                log.info("DevMachine {}/{} has stopped", namespace, name);
                break;
            case "Failed":
                log.error("DevMachine {}/{} failed: {}", namespace, name, message);
                // 可以在这里触发告警
                break;
            default:
                log.warn("Unknown phase for DevMachine {}/{}: {}", namespace, name, phase);
        }
    }

    @PreDestroy
    public void stopWatching() {
        log.info("Stopping DevMachine watcher...");
        if (watch != null) {
            try {
                watch.close();
                log.info("DevMachine watcher stopped");
            } catch (IOException e) {
                log.error("Error closing watcher", e);
            }
        }
    }
}