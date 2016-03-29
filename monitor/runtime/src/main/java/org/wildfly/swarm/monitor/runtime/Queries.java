package org.wildfly.swarm.monitor.runtime;

/**
 * @author Heiko Braun
 * @since 21/03/16
 */
class Queries {

    /*public final static boolean isHealthEndpoint(Monitor monitor, String relativePath) {
        return query(monitor, metaData -> {
            return relativePath.equals(HttpContexts.HEALTH+metaData.getWebContext());
        });
    }*/

    public final static boolean isSecuredHealthEndpoint(Monitor monitor, String relativePath) {
        return query(monitor, metaData -> {
            return relativePath.equals(HttpContexts.HEALTH+metaData.getWebContext()) && metaData.isSecure();
        });
    }

    public final static boolean preventDirectAccess(Monitor monitor, String relativePath) {
        return query(monitor, metaData -> {
            return relativePath.equals(metaData.getWebContext()) && metaData.isSecure();
        });
    }

    public final static boolean query(Monitor monitor, Condition condition) {
        boolean isCondition = false;
        for(HealthMetaData metaData : monitor.getHealthURIs()){
            isCondition = condition.eval(metaData);
            if(isCondition) break;
        }

        return isCondition;
    }

    @FunctionalInterface
    public interface Condition {
        boolean eval(HealthMetaData metaData);
    }


}
