package com.ahmadamirizal.leave.web.interceptor;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.struts2.ActionInvocation;
import org.apache.struts2.interceptor.AbstractInterceptor;

import com.ahmadamirizal.leave.model.Role;
import com.ahmadamirizal.leave.model.User;
import com.ahmadamirizal.leave.web.BaseAction;

/** Restricts a package of actions to some roles, e.g. {@code <param name="roles">ADMIN,HR</param>}. Runs after auth. */
public class RoleInterceptor extends AbstractInterceptor {

    private Set<Role> roles = Set.of();

    public void setRoles(String roles) {
        this.roles = Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Role::valueOf)
                .collect(Collectors.toSet());
    }

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        User user = invocation.getAction() instanceof BaseAction a ? a.getCurrentUser() : null;
        if (user == null || !roles.contains(user.getRole())) {
            return "forbidden";
        }
        return invocation.invoke();
    }
}
