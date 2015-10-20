/*
 * Copyright 2015 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.swarm.arquillian.adapter;

import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps track of transfers, and can provide failure messages for
 * resources that aren't found in any repo.
 */
public class FailureReportingTransferListener extends AbstractTransferListener implements CompleteableTransferListener {

    @Override
    public void transferSucceeded(final TransferEvent event) {
        this.transfers.remove(event.getResource());
    }

    @Override
    public void transferFailed(final TransferEvent event) {
        final TransferResource resource = event.getResource();
        List<TransferEvent> events = this.transfers.get(resource);
        if (events == null) {
            events = new ArrayList<>();
            this.transfers.put(resource, events);
        }
        events.add(event);
    }

    @Override
    public void complete() {
        if (hasFailures()) {
            System.err.print(failuresAsString());
            this.transfers.clear();
        }
    }

    public boolean hasFailures() {
        return !this.transfers.isEmpty();
    }

    public String failuresAsString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<TransferResource, List<TransferEvent>> entry : this.transfers.entrySet()) {
            TransferResource resource = entry.getKey();
            for (TransferEvent event : entry.getValue()) {
                sb.append("Failed")
                        .append(event.getRequestType() == TransferEvent.RequestType.PUT ? " uploading " : " downloading ")
                        .append(resource.getResourceName())
                        .append(event.getRequestType() == TransferEvent.RequestType.PUT ? " into " : " from ")
                        .append(resource.getRepositoryUrl()).append(". ");
                if (event.getException() != null) {
                    sb.append("Reason: \n").append(event.getException());
                }
                sb.append('\n');
            }
        }

        return sb.toString();
    }

    private final Map<TransferResource, List<TransferEvent>> transfers = new HashMap<>();
}
