package com.ahmadamirizal.leave.web.action;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.parameter.StrutsParameter;

import com.ahmadamirizal.leave.model.User;
import com.ahmadamirizal.leave.service.LoginThrottle;
import com.ahmadamirizal.leave.web.BaseAction;

public class LoginAction extends BaseAction {

    private String username;
    private String password;

    /** GET shows the form (or goes straight to the dashboard if already signed in); POST signs in. */
    @Override
    public String execute() {
        HttpServletRequest request = ServletActionContext.getRequest();
        if (!"POST".equals(request.getMethod())) {
            return session().get(SESSION_USER_ID) != null ? SUCCESS : INPUT;
        }
        LoginThrottle throttle = services().loginThrottle;
        if (throttle.isLocked(username)) {
            addActionError("Too many wrong passwords. Try again in " + LoginThrottle.LOCK.toMinutes() + " minutes.");
            return INPUT;
        }
        Optional<User> user = services().auth.login(username, password);
        password = null;
        if (user.isEmpty()) {
            throttle.failed(username);
            addActionError("Wrong username or password.");
            return INPUT;
        }
        throttle.succeeded(username);
        // A new session id at login, so an id planted before login (session fixation) is useless afterwards.
        request.changeSessionId();
        session().put(SESSION_USER_ID, user.get().getId());
        return SUCCESS;
    }

    public String getUsername() { return username; }

    @StrutsParameter
    public void setUsername(String username) { this.username = username; }

    @StrutsParameter
    public void setPassword(String password) { this.password = password; }
}
