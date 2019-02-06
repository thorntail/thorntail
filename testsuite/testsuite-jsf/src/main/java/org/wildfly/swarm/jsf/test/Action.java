package org.wildfly.swarm.jsf.test;

import javax.enterprise.inject.Model;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

@Model
public class Action {
    public void perform() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Action message"));
    }
}
