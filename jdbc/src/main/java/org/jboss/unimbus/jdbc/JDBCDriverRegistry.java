package org.jboss.unimbus.jdbc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by bob on 2/1/18.
 */
@ApplicationScoped
public class JDBCDriverRegistry implements Iterable<DriverInfo> {

    @PostConstruct
    private void register() {
        for (DriverInfo driverInfo : this.driverInfos) {
            register(driverInfo);
        }
    }

    public JDBCDriverRegistry() {

    }

    public void register(DriverInfo driver) {
        this.drivers.put( driver.getId(), driver );
        JDBCMessages.MESSAGES.registeredDriver(driver.getId());
    }

    public void unregister(DriverInfo driver) {
        this.drivers.remove(driver.getId());
    }

    public DriverInfo get(String id) {
        return this.drivers.get(id);
    }

    @Override
    public Iterator<DriverInfo> iterator() {
        return this.drivers.values().iterator();
    }

    public int size() {
        return this.drivers.size();
    }

    private Map<String,DriverInfo> drivers = new HashMap<>();

    @Inject
    @Any
    Instance<DriverInfo> driverInfos;

}
