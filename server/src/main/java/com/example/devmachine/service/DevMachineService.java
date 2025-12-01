package com.example.devmachine.service;

import com.example.devmachine.controller.DevMachineController;
import com.example.devmachine.model.crd.DevMachine;
import com.example.devmachine.model.crd.DevMachineSpec;
import com.google.gson.Gson;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.PatchContext;
import io.fabric8.kubernetes.client.dsl.base.PatchType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author maqidi
 * @version 1.0
 * @create 2025-11-21 19:37
 */
@Service
@Slf4j
public class DevMachineService {
    @Autowired
    private KubernetesClient kubernetesClient;

    private static final String FIELD_MANAGER = "devmachine-java-backend";


    private MixedOperation<DevMachine, KubernetesResourceList<DevMachine>, Resource<DevMachine>> devMachineClient;

    @PostConstruct
    public void init() {
        devMachineClient = kubernetesClient.resources(DevMachine.class);
    }

    /**
     * 使用 Server-Side Apply 创建或更新 DevMachine
     */
    public DevMachine createOrUpdateDevMachine(DevMachine devMachine) {
        log.info(kubernetesClient.getNamespace());
        log.info("Creating/Updating DevMachine: {}/{}",
                devMachine.getMetadata().getNamespace(),
                devMachine.getMetadata().getName());

        // 使用 Server-Side Apply
        // 注意：Fabric8 6.x 的 serverSideApply API
        try {
            DevMachine result = devMachineClient
                    .inNamespace(devMachine.getMetadata().getNamespace())
                    .resource(devMachine)
                    .fieldManager(FIELD_MANAGER)
                    .serverSideApply();

            log.info("DevMachine created/updated: {}", result.getMetadata().getName());
            return result;
        } catch (Exception e) {
            log.error("Failed to apply DevMachine", e);
            throw new RuntimeException("Failed to apply DevMachine", e);
        }
    }

    /**
     * 更新 DevMachine spec（完整替换）
     */
    public DevMachine updateDevMachineSpec(String namespace, String name, DevMachineSpec spec) {
        log.info("Updating DevMachine spec: {}/{}", namespace, name);

        DevMachine existing = getDevMachine(namespace, name);
        if (existing == null) {
            throw new RuntimeException("DevMachine not found: " + namespace + "/" + name);
        }
        //不需要的mata内容不在更新，避免：更新出错
        ObjectMeta meta = new ObjectMeta();
        meta.setName(name);
        meta.setNamespace(namespace);
        DevMachine devMachine = new DevMachine();
        devMachine.setMetadata(meta);
        devMachine.setSpec(spec);
        //就不分更新，不要替换
        return createOrUpdateDevMachine(devMachine);
    }

    /**
     * 获取 DevMachine
     */
    public DevMachine getDevMachine(String namespace, String name) {
        return devMachineClient
                .inNamespace(namespace)
                .withName(name)
                .get();
    }

    /**
     * 列出 DevMachines
     */
    public List<DevMachine> listDevMachines(String namespace) {
        return devMachineClient
                .inNamespace(namespace)
                .list()
                .getItems();
    }

    /**
     * 删除 DevMachine
     */
    public void deleteDevMachine(String namespace, String name) {
        log.info("Deleting DevMachine: {}/{}", namespace, name);
        try {
            List<StatusDetails> statusDetails = devMachineClient
                    .inNamespace(namespace)
                    .withName(name)
                    .delete();
            log.info("Deleting DevMachine status: {}", new Gson().toJson(statusDetails));
        } catch (Exception e) {
            log.error("Failed to delete DevMachine", e);
        }

    }

}
