package com.example.devmachine.model.crd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 因为存在未知的字段所以加@JsonIgnoreProperties(ignoreUnknown = true)
 * @author maqidi
 * @version 1.0
 * @create 2025-11-21 19:37
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DevMachineStatus {
    @JsonProperty("phase")
    private String phase; // Pending, Creating, Running, Stopping, Stopped, Failed

    @JsonProperty("message")
    private String message;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("lastTransitionTime")
    private String lastTransitionTime;

    @JsonProperty("conditions")
    private List<Condition> conditions;

    @JsonProperty("podName")
    private String podName;

    @JsonProperty("podIP")
    private String podIP;

    @JsonProperty("ingressURL")
    private String ingressURL;

    @Data
    public static class Condition {
        private String type;
        private String status; // True, False, Unknown
        private String lastTransitionTime;
        private String reason;
        private String message;
    }

}
