package com.example.devmachine.model.crd;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Plural;
import io.fabric8.kubernetes.model.annotation.Version;

/**
 * @author maqidi
 * @version 1.0
 * @create 2025-11-21 19:37
 */
@Group(DevMachine.GROUP)
@Version("v1alpha1")
@Kind("DevMachine")
@Plural("devmachines")
public class DevMachine extends CustomResource<DevMachineSpec, DevMachineStatus> implements Namespaced {
    public static final String GROUP = "ide.example.com";
    //name从meta里面传值
}
