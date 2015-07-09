package com.alfresco.demo.bpm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.UrlUtil;
import org.springframework.stereotype.Component;

@Component
public class ApprovalNotificationListener implements TaskListener {

    private ServiceRegistry serviceRegistry;

    private static final long serialVersionUID = 1L;

    private static final String SUBJECT = "Policy Approval Task Notification";

    private static final String FROM_ADDRESS = "noreply@alfresco.com";

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
	this.serviceRegistry = serviceRegistry;
    }

    public void notify(DelegateTask task) {

	String assignee = task.getAssignee();

	NodeRef person = serviceRegistry.getPersonService().getPerson(assignee);
	String email = (String) serviceRegistry.getNodeService().getProperty(
		person, ContentModel.PROP_EMAIL);

	StringBuffer sb = new StringBuffer();
	sb.append("You have been assigned to a task named ");
	sb.append(task.getName());

	Map<String, Object> model = new HashMap<String, Object>();

	model.put("shareUrl",
		UrlUtil.getShareUrl(serviceRegistry.getSysAdminParams()));
	model.put("date", new Date());
	model.put("task", task);
	model.put("person", person);

	String emailBody = serviceRegistry
		.getTemplateService()
		.processTemplate(
			"alfresco/module/policy-demo-server/workflow/templates/notification.ftl",
			model);

	ActionService actionService = serviceRegistry.getActionService();
	Action mailAction = actionService.createAction(MailActionExecuter.NAME);
	mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT,
		ApprovalNotificationListener.SUBJECT);
	mailAction.setParameterValue(MailActionExecuter.PARAM_TO, email);
	mailAction.setParameterValue(MailActionExecuter.PARAM_FROM,
		ApprovalNotificationListener.FROM_ADDRESS);
	mailAction.setParameterValue(MailActionExecuter.PARAM_TEXT, emailBody);
	mailAction.setParameterValue(
		MailActionExecuter.PARAM_IGNORE_SEND_FAILURE, Boolean.TRUE);

	actionService.executeAction(mailAction, null);
    }
}
