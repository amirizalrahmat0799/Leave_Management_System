package com.ahmadamirizal.leave.web;

import java.util.Map;

import org.apache.struts2.ActionContext;
import org.apache.struts2.ActionSupport;

import com.ahmadamirizal.leave.model.User;
import com.ahmadamirizal.leave.service.Services;

/** Common to every page behind the login: the signed-in user and a one-time message shown after a redirect. */
public abstract class BaseAction extends ActionSupport {

    public static final String SESSION_USER_ID = "userId";
    public static final String SESSION_FLASH = "flash";

    private User currentUser;

    /** Set by {@link com.ahmadamirizal.leave.web.interceptor.AuthInterceptor} before the action runs. */
    public void setCurrentUserFromInterceptor(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    protected Services services() {
        return Services.get();
    }

    protected Map<String, Object> session() {
        return ActionContext.getContext().getSession();
    }

    /** A message for the next page, which is usually reached by a redirect. */
    protected void flash(String message) {
        session().put(SESSION_FLASH, message);
    }
}
