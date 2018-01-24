package org.jboss.unimbus.servlet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by bob on 1/24/18.
 */
public class SecurityConstraintMetaData {

    public SecurityConstraintMetaData addWebResourceCollection(WebResourceCollectionMetaData collection) {
        this.webResourceCollections.add( collection );
        return this;
    }

    public List<WebResourceCollectionMetaData> getWebResourceCollections() {
        return this.webResourceCollections;
    }

    public SecurityConstraintMetaData addRoleAllowed(String role) {
        this.rolesAllowed.add( role );
        return this;
    }

    public Set<String> getRolesAllowed() {
        return this.rolesAllowed;
    }

    public SecurityConstraintMetaData setEmptyRoleSemantic(ServletSecurityMetaData.EmptyRoleSemantic emptyRoleSemantic) {
        this.emptyRoleSemantic = emptyRoleSemantic;
        return this;
    }

    public ServletSecurityMetaData.EmptyRoleSemantic getEmptyRoleSemantic() {
        return emptyRoleSemantic;
    }

    private List<WebResourceCollectionMetaData> webResourceCollections = new ArrayList<>();

    private Set<String> rolesAllowed = new HashSet<>();

    private ServletSecurityMetaData.EmptyRoleSemantic emptyRoleSemantic = ServletSecurityMetaData.EmptyRoleSemantic.PERMIT;
}
