// Copyright 2019 Oath Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.hosted.provision.provisioning;

import com.yahoo.config.provision.ClusterSpec;
import com.yahoo.config.provision.Environment;
import com.yahoo.config.provision.NodeResources;
import com.yahoo.config.provision.SystemName;
import com.yahoo.config.provision.Zone;
import com.yahoo.vespa.hosted.provision.Node;
import com.yahoo.vespa.hosted.provision.NodeRepository;

import java.util.Locale;

/**
 * Defines the resource limits for nodes in various zones
 *
 * @author bratseth
 * @see CapacityPolicies
 */
public class NodeResourceLimits {

    private final NodeRepository nodeRepository;

    public NodeResourceLimits(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    /** Validates the resources applications ask for (which are in "advertised" resource space) */
    public void ensureWithinAdvertisedLimits(String type, NodeResources requested, ClusterSpec cluster) {
        if (requested.isUnspecified()) return;

        if (requested.vcpu() < minAdvertisedVcpu(cluster.type()))
            illegal(type, "vcpu", "", cluster, requested.vcpu(), minAdvertisedVcpu(cluster.type()));
        if (requested.memoryGb() < minAdvertisedMemoryGb(cluster.type()))
            illegal(type, "memoryGb", "Gb", cluster, requested.memoryGb(), minAdvertisedMemoryGb(cluster.type()));
        if (requested.diskGb() < minAdvertisedDiskGb(requested))
            illegal(type, "diskGb", "Gb", cluster, requested.diskGb(), minAdvertisedDiskGb(requested));
    }

    /** Returns whether the real resources we'll end up with on a given tenant node are within limits */
    public boolean isWithinRealLimits(Node candidateTenantNode, ClusterSpec cluster) {
        return isWithinRealLimits(nodeRepository.resourcesCalculator().realResourcesOf(candidateTenantNode, nodeRepository),
                                  cluster.type());
    }

    /** Returns whether the real resources we'll end up with on a given tenant node are within limits */
    public boolean isWithinRealLimits(NodeResources realResources, ClusterSpec.Type clusterType) {
        if (realResources.isUnspecified()) return true;

        if (realResources.vcpu() < minRealVcpu(clusterType)) return false;
        if (realResources.memoryGb() < minRealMemoryGb(clusterType)) return false;
        if (realResources.diskGb() < minRealDiskGb()) return false;
       return true;
    }

    public NodeResources enlargeToLegal(NodeResources requested, ClusterSpec.Type clusterType) {
        if (requested.isUnspecified()) return requested;

        return requested.withVcpu(Math.max(minAdvertisedVcpu(clusterType), requested.vcpu()))
                        .withMemoryGb(Math.max(minAdvertisedMemoryGb(clusterType), requested.memoryGb()))
                        .withDiskGb(Math.max(minAdvertisedDiskGb(requested), requested.diskGb()));
    }

    private double minAdvertisedVcpu(ClusterSpec.Type clusterType) {
        if (zone().environment() == Environment.dev && zone().getCloud().allowHostSharing()) return 0.1;
        return 0.5;
    }

    private double minAdvertisedMemoryGb(ClusterSpec.Type clusterType) {
        if (zone().system() == SystemName.dev) return 1; // Allow small containers in dev system
        if (clusterType == ClusterSpec.Type.admin) return 2;
        return 4;
    }

    private double minAdvertisedDiskGb(NodeResources requested) {
        if (requested.storageType() == NodeResources.StorageType.local && zone().getCloud().dynamicProvisioning()) {
            if (zone().system() == SystemName.Public)
                return 10 + minRealDiskGb();
            else
                return 55 + minRealDiskGb();
        }
        return 4 + minRealDiskGb();
    }

    private double minRealVcpu(ClusterSpec.Type clusterType) {
        return minAdvertisedVcpu(clusterType);
    }

    private double minRealMemoryGb(ClusterSpec.Type clusterType) {
        return minAdvertisedMemoryGb(clusterType) - 1.7;
    }

    private double minRealDiskGb() {
        return 6;
    }

    private Zone zone() { return nodeRepository.zone(); }

    private void illegal(String type, String resource, String unit, ClusterSpec cluster, double requested, double minAllowed) {
        if ( ! unit.isEmpty())
            unit = " " + unit;
        String message = String.format(Locale.ENGLISH,
                                       "%s cluster '%s': " + type + " " + resource +
                                       " size is %.2f%s but must be at least %.2f%s",
                                       cluster.type().name(), cluster.id().value(), requested, unit, minAllowed, unit);
        throw new IllegalArgumentException(message);
    }

}
