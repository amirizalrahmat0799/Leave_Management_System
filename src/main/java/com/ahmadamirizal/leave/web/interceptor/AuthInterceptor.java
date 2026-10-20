package com.ahmadamirizal.leave.web.interceptor;

import java.util.Map;
import java.util.Optional;

import org.apache.struts2.ActionInvocation;
import org.apache.struts2.interceptor.AbstractInterceptor;

import com.ahmadamirizal.leave.model.User;
import com.ahmadamirizal.leave.service.Services;
import com.ahmadamirizal.leave.web.BaseAction;
import com.ahmadamirizal.leave.web.action.PasswordAction;

/**
 * Lets a request through only when someone is signed in. The user is reloaded from the database on every request,
 * so a deactivated account or a changed role takes effect immediately, not at the next login.
 * Users who must change their password are sent to that page first.
 */
public class AuthInterceptor extends AbstractInterceptor {

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        Map<String, Object> session = invocation.getInvocationContext().getSession();
        Object id = session.get(BaseAction.SESSION_USER_ID);
        Optional<User> user = id instanceof Integer userId ? Services.get().users.find(userId) : Optional.empty();
        if (user.isEmpty() || !user.get().isActive()) {
            session.remove(BaseAction.SESSION_USER_ID);
            return "login";
        }
        if (invocation.getAction() instanceof BaseAction action) {
            action.setCurrentUserFromInterceptor(user.get());
        }
        if (user.get().isMustChangePassword() && !(invocation.getAction() instanceof PasswordAction)) {
            return "changePassword";
        }
        return invocation.invoke();
    }
}
