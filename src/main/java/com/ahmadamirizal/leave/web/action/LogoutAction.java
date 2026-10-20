package com.ahmadamirizal.leave.web.action;

import jakarta.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;

import com.ahmadamirizal.leave.web.BaseAction;

public class LogoutAction extends BaseAction {

    @Override
    public String execute() {
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        if (session != null) session.invalidate();
        return SUCCESS;
    }
}
