package io.thorntail.servlet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Security constraint descriptor.
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 */
public class SecurityConstraintMetaData {

    /**
     * Construct.
     */
    public SecurityConstraintMetaData() {
        this.emptyRoleSemantic = EmptyRoleSemantic.PERMIT;
    }

    /**
     * Add a web resource collection.
     *
     * @param collection The web resource collection.
     * @return This meta-data object.
     */
    public SecurityConstraintMetaData addWebResourceCollection(WebResourceCollectionMetaData collection) {
        this.webResourceCollections.add(collection);
        return this;
    }

    /**
     * Retrieve the web resource collections.
     *
     * @return The web resource collections.
     */
    public List<WebResourceCollectionMetaData> getWebResourceCollections() {
        return this.webResourceCollections;
    }

    /**
     * Add an allowed role.
     *
     * @param role The role.
     * @return This meta-data object.
     * @see #setEmptyRoleSemantic(EmptyRoleSemantic)
     */
    public SecurityConstraintMetaData addRoleAllowed(String role) {
        this.rolesAllowed.add(role);
        return this;
    }

    /**
     * Retrieve the allowed roles.
     *
     * @return The allowed roles.
     * @see #getEmptyRoleSemantic()
     */
    public Set<String> getRolesAllowed() {
        return this.rolesAllowed;
    }

    /**
     * Set the empty-role semantic.
     *
     * @param emptyRoleSemantic The empty-role semantic.
     * @return This meta-data object.
     */
    public SecurityConstraintMetaData setEmptyRoleSemantic(EmptyRoleSemantic emptyRoleSemantic) {
        this.emptyRoleSemantic = emptyRoleSemantic;
        return this;
    }

    /**
     * Retrieve the empty-role semantic.
     *
     * @return The empty-role semantic.
     */
    public EmptyRoleSemantic getEmptyRoleSemantic() {
        return emptyRoleSemantic;
    }

    private List<WebResourceCollectionMetaData> webResourceCollections = new ArrayList<>();

    private Set<String> rolesAllowed = new HashSet<>();

    private EmptyRoleSemantic emptyRoleSemantic = EmptyRoleSemantic.PERMIT;
}
