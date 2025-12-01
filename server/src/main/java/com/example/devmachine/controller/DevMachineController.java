package com.example.devmachine.controller;

import com.example.devmachine.model.crd.DevMachine;
import com.example.devmachine.model.crd.DevMachineStatus;
import com.example.devmachine.service.DevMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 因为helm的渲染偏向于创建而不是单个更新，所以做好使用的维护方式，使用helm创建
 * 如果需要维护字段，需要自身的go那种
 * <p>
 * 本类之所以不写update一类的操作，因为helm部署模版不适合更新其中几个字段，带来全内容的upgrade
 *
 * @author maqidi
 * @version 1.0
 * @create 2025-11-21 19:36
 */
@RestController
@RequestMapping("/api/v1/devmachines")
public class DevMachineController {

    @Autowired
    private DevMachineService devMachineService;

    /**
     * 创建新的 DevMachine（使用 server-side apply）
     */
    @PostMapping("/create")
    public ResponseEntity<DevMachine> createDevMachine(
            @RequestParam(defaultValue = "default") String namespace,
            @RequestBody DevMachine devMachine) {

        devMachine.getMetadata().setNamespace(namespace);
        DevMachine created = devMachineService.createOrUpdateDevMachine(devMachine);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 获取 DevMachine（包含最新 status）
     */
    @GetMapping("/get")
    public ResponseEntity<DevMachine> getDevMachine(
            @RequestParam String name,
            @RequestParam(defaultValue = "default") String namespace) {

        DevMachine devMachine = devMachineService.getDevMachine(namespace, name);
        if (devMachine == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(devMachine);
    }

    /**
     * 获取 DevMachine status
     */
    @GetMapping("/status")
    public ResponseEntity<DevMachineStatus> getDevMachineStatus(
            @RequestParam String name,
            @RequestParam(defaultValue = "default") String namespace) {

        DevMachine devMachine = devMachineService.getDevMachine(namespace, name);
        if (devMachine == null || devMachine.getStatus() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(devMachine.getStatus());
    }

    /**
     * 列出所有 DevMachines
     */
    @GetMapping("/list")
    public ResponseEntity<List<DevMachine>> listDevMachines(
            @RequestParam(defaultValue = "default") String namespace) {

        List<DevMachine> list = devMachineService.listDevMachines(namespace);
        return ResponseEntity.ok(list);
    }

    /**
     * 删除 DevMachine
     */
    @GetMapping("/delete")
    public ResponseEntity<Void> deleteDevMachine(
            @RequestParam String name,
            @RequestParam(defaultValue = "default") String namespace) {

        devMachineService.deleteDevMachine(namespace, name);
        return ResponseEntity.noContent().build();
    }
}
