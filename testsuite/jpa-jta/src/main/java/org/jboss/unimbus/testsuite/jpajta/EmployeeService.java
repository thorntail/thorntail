/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * by the @authors tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.unimbus.testsuite.jpajta;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.logging.Logger;

/**
 *
 * Service bean used by {@link EmployeeRest}
 *
 */
@ApplicationScoped
public class EmployeeService {

    @PersistenceContext
    private EntityManager em;

    @Inject
    Event<Employee> empvt;

    static final Logger LOG = Logger.getLogger(EmployeeService.class);


    public List<Employee> getAll() {
        List<Employee> emps = em.createNamedQuery("Employee.findAll", Employee.class).getResultList();
    return emps;
    }


    public void remTx(Integer idToFailOn) {

        for (Integer i = 1; i < 9; i++) {
            Employee employee = em.find(Employee.class, i);
            LOG.info("Fetch emp # " + employee.getId());
            //empvt.fire(employee); // TODO: Make transactional observers work
            em.remove(employee);
            LOG.info("EM is joined trans: " + em.isJoinedToTransaction());
            if (i.equals(idToFailOn)) {
                throw new IllegalStateException("Random failure");
            }

        }

    }

    public Employee create(Employee employee) {
        return em.merge(employee);
    }

    public String hello() { return "hello from service";}
}
