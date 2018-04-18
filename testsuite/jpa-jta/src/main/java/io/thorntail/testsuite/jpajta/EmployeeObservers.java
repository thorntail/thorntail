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

package io.thorntail.testsuite.jpajta;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;

import org.jboss.logging.Logger;

/**
 *
 * Bean for testing transactional observers
 *
 * @author Antoine Sabot-Durand
 */
@ApplicationScoped
public class EmployeeObservers {

    final static Logger LOG = Logger.getLogger(EmployeeObservers.class);

    void processTxFailure(@Observes(during = TransactionPhase.AFTER_FAILURE) Employee emp) {
        LOG.info("*** An error occured and deletion of emp # " + emp.getId()+ " was rollbacked");
    }

    void processTxSuccess(@Observes(during = TransactionPhase.AFTER_SUCCESS) Employee emp) {
        LOG.info("*** Deletion of emp # " + emp.getId()+ " was committed");
    }


}
