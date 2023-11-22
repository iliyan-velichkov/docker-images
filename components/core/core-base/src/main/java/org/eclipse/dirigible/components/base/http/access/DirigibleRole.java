package org.eclipse.dirigible.components.base.http.access;

public enum DirigibleRole {

    DEVELOPER("DEVELOPER"), OPERATOR("OPERATOR");

    private final String roleName;

    DirigibleRole(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}
