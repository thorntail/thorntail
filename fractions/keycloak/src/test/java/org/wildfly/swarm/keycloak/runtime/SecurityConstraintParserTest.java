package org.wildfly.swarm.keycloak.runtime;

import org.junit.Test;
import org.wildfly.swarm.undertow.descriptors.SecurityConstraint;

import static org.fest.assertions.Assertions.assertThat;

public class SecurityConstraintParserTest {

    @Test
    public void set_url_pattern_and_methods_and_roles_explicitly() throws Exception {
        String securityConstraint = "{url-pattern=/aaa, methods=[GET], roles=[role-a]}";

        SecurityConstraint result = SecurityConstraintParser.parse(securityConstraint);

        assertThat(result.urlPattern()).isEqualTo("/aaa");
        assertThat(result.methods()).contains("GET");
        assertThat(result.roles()).contains("role-a");
    }

    @Test
    public void unconcerned_with_order_of_definition() throws Exception {
        String securityConstraint = "{roles=[role-a], methods=[GET], url-pattern=/aaa}";

        SecurityConstraint result = SecurityConstraintParser.parse(securityConstraint);

        assertThat(result.urlPattern()).isEqualTo("/aaa");
        assertThat(result.methods()).contains("GET");
        assertThat(result.roles()).contains("role-a");
    }

    @Test
    public void set_several_values_if_methods_or_roles_has_them() throws Exception {
        String securityConstraint = "{url-pattern=/aaa, methods=[GET, PUT], roles=[role-a, role-b]}";

        SecurityConstraint result = SecurityConstraintParser.parse(securityConstraint);

        assertThat(result.methods()).contains("GET", "PUT");
        assertThat(result.roles()).contains("role-a", "role-b");
    }

    @Test
    public void set_wild_card_as_default_url_pattern() throws Exception {
        String securityConstraint = "{methods=[GET], roles=[role-a]}";

        SecurityConstraint result = SecurityConstraintParser.parse(securityConstraint);

        assertThat(result.urlPattern()).isEqualTo("/*");
    }

    @Test
    public void set_no_value_if_methods_or_roles_not_specified() throws Exception {
        String securityConstraint = "{url-pattern=/aaa}";

        SecurityConstraint result = SecurityConstraintParser.parse(securityConstraint);

        assertThat(result.methods()).hasSize(0);
        assertThat(result.roles()).hasSize(0);
    }

}
