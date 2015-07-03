package com.alfresco.demo.bpm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowNotificationUtils;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.UrlUtil;
import org.apache.chemistry.opencmis.server.support.filter.JsonPrettyPrinter;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

@Component
public class PolicyApprovalMessageListener implements ExecutionListener {

    private static final long serialVersionUID = 1L;

    private ServiceRegistry serviceRegistry;

    private final JmsTemplate jmsTemplate;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
	this.serviceRegistry = serviceRegistry;
    }

    @Autowired
    public PolicyApprovalMessageListener(final JmsTemplate jmsTemplate) {
	this.jmsTemplate = jmsTemplate;
    }

    @Override
    public void notify(DelegateExecution execution) throws Exception {

	ActivitiScriptNode scriptNode = (ActivitiScriptNode) execution
		.getVariable(WorkflowNotificationUtils.PROP_PACKAGE);

	NodeRef packagenode = scriptNode.getNodeRef();
	NodeRef nodeRef = serviceRegistry.getNodeService()
		.getChildAssocs(packagenode).get(0).getChildRef();

	String name = (String) serviceRegistry.getNodeService().getProperty(
		nodeRef, ContentModel.PROP_NAME);
	
	String url = UrlUtil.getShareUrl(serviceRegistry.getSysAdminParams())
		+ "/page/site/"
		+ serviceRegistry.getSiteService().getSite(nodeRef)
			.getShortName() + "/document-details?nodeRef="
		+ nodeRef.toString();

	jmsTemplate.send("alfresco.policy.approval", new MessageCreator() {
	    public Message createMessage(Session session) throws JMSException {

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("event", "Policy document " + name + " approved");
		map.put("url", url);
		map.put("date", new Date());
		JsonPrettyPrinter printer = new JsonPrettyPrinter(2);
		return session.createTextMessage(printer.prettyPrint(JSONObject
			.toJSONString(map)));

	    }
	});
    }

}
