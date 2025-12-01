package com.example.devmachine.model.crd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author maqidi
 * @version 1.0
 * @create 2025-11-21 19:37
 */
@Data
@NoArgsConstructor
public class DevMachineSpec {

    @JsonProperty("cpu")
    private Integer cpu;

    @JsonProperty("memory")
    private String memory;

    @JsonProperty("gpu")
    private Integer gpu = 0;

    @JsonProperty("image")
    private String image;

    @JsonProperty("template")
    private String template;

    @JsonProperty("ingressDomain")
    private String ingressDomain;

    @JsonProperty("storageSize")
    private String storageSize = "10Gi";

    @JsonProperty("mounts")
    private List<Mount> mounts = new ArrayList<>();


    @JsonProperty("envMap")
    private Map<String, String> envMap;

    private List<String> command;

    @Data
    @NoArgsConstructor
    public static class Mount {
        /**
         * 是hpfs还是s3 这样的选择
         */
        @JsonProperty("type")
        private String type;

        @JsonProperty("bucket")
        private String bucket;
        /**
         * 原位置与挂载的位置
         */
        @JsonProperty("path")
        private String path;

        @JsonProperty("mountPath")
        private String mountPath;
    }
}
