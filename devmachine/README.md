# DevMachine Operator

完整的 Kubernetes Operator 项目，使用 Operator SDK (Helm 插件) 管理开发机环境。

Helm的特性决定了，它只适合全量的部署更新，所以这里不再纠结更新cpu和gpu等参数，直接删除和重新部署

## 架构
```
Java Backend (声明层) → DevMachine CRD (期望状态) → Helm Operator (控制层)
```
- **Java Backend**: 使用 Fabric8 和 Server-Side Apply 仅写入 CRD.spec
- **Helm Operator**: 通过 Helm chart 渲染并管理 Deployment/Service/Ingress/PVC
- **CRD**: 定义开发机的期望状态和实际状态
## 功能特性

- ✅ 完整的 CRD 定义（含 validation schema）
- ✅ 基于 Helm 的 Operator（无需编写 Go 代码）
- ✅ 灵活的资源配置（CPU、内存、GPU）
- ✅ 多种挂载类型支持（S3、NFS、PVC）
- ✅ 自动生成 Ingress 访问入口
- ✅ Java 后端使用 Server-Side Apply
- ✅ 实时监听 status 变化
- ✅ 支持开发机模板（vscode、jupyter 等）

### Server-Side Apply 优势

1. **冲突管理**: 通过 FieldManager 追踪字段所有权
2. **部分更新**: 只更新变化的字段
3. **幂等性**: 重复 apply 不会产生副作用
4. **多控制器协作**: 不同控制器可管理同一资源的不同字段

### 前置条件

- Kubernetes 1.22+
- kubectl
- Operator SDK 1.32+
- Docker
- Java 17+ & Maven 3.8+
- Helm 3.x

### 1. 部署 CRD
```bash
# 创建命名空间
kubectl create namespace devmachine-system
# 创建crd
kubectl apply -f crd/devmachine-crd.yaml
```
验证 CRD 已创建：
```bash
kubectl get crd devmachines.ide.example.com
```
### 2. 构建并部署 Operator
1. 配置确定[manager.yaml](config%2Fmanager%2Fmanager.yaml)和[service_account.yaml](config%2Frbac%2Fservice_account.yaml)的命名空间一致namespace都是devmachine-system
2. 在同命名空间devmachine-system的资源下，创建name为docker-login的secret
```bash
cd secret
kubectl apply -f dock_login_secret.yaml
```
3. 因为manager.yaml引用了service_account，所以在ServiceAccount中定义拉取镜像的账号，根据名字在相同的namespace下找secret资源：
```yaml
imagePullSecrets: # 这里写入docker的登录
  - name: docker-login
```


```bash
cd devmachine 

# 构建 Operator 镜像，使用podman
#手动创建namespace
kubectl create namespace devmachine-system


make docker-build docker-push IMG=harbor.ctyuncdn.cn/mlspace/devmachine/devmachine-operator:v0.0.1 CONTAINER_TOOL=podman

make deploy IMG=harbor.ctyuncdn.cn/mlspace/devmachine/devmachine-operator:v0.0.1 CONTAINER_TOOL=podman
```
## API 使用示例

因为使用的时候需要注意公司内网和外网的区别，通过kube本地配置查看有无权限即可使用了

### 获取 DevMachine
```bash
http://localhost:8080/api/v1/devmachines/status?name=my-devmachine&namespace=taskmodeling-public-prod
```


```bash
#创建开发机

curl --location 'http://localhost:8080/api/v1/devmachines?namespace=taskmodeling-public-prod' \
--header 'Content-Type: application/json' \
--data '{
    "apiVersion": "ide.example.com/v1alpha1",
    "kind": "DevMachine",
    "metadata": {
      "name": "my-devmachine"
    },
    "spec": {
      "cpu": 2,
      "memory": "4Gi",
      "gpu": 0,
      "image": "registry-test.ctyun.cn:30443/inner/ubt22-cuda12.4-py310-torch2.6.0-gpu-tdsw-swift-megatron:3.0",
      "template": "vscode-cpu",
      "ingressDomain": "mydev.example.com",
      "storage": {
        "size": "20Gi"
      }
    }
  }'

# 更新开发机

```


### 4.清理
```bash
# 删除所有 DevMachine 实例
kubectl delete devmachines --all

# 卸载 Operator
make undeploy
# 或
kubectl delete -f devmachine/config/

# 删除 CRD
kubectl delete -f crd/devmachine-crd.yaml
```

## 已知限制与注意事项

1. **Status 更新**: Helm Operator 不直接支持复杂的 status 更新逻辑，需要通过 hooks 或额外的 controller 实现

2. **Stop 操作**: Helm 没有内置的"停止"概念，需要通过删除 CR 或添加自定义字段（如 `stopped: true`）配合 chart 逻辑实现

3. **S3/HFPS 挂载**: 需要集群安装相应的 CSI 驱动或使用 init container + sidecar 方案

4. **GPU 调度**: 需要集群安装 NVIDIA Device Plugin 或其他 GPU 调度组件

5. **Ingress TLS**: 需要集群安装 cert-manager 或手动管理证书





