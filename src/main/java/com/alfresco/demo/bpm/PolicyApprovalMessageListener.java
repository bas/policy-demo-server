package com.alfresco.demo.bpm;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;

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
import org.alfresco.util.UrlUtil;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
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

	jmsTemplate.send("alfresco.policy.approval", new MessageCreator() {
	    public Message createMessage(Session session) throws JMSException {
		try {
		    return session.createTextMessage(getFeed(nodeRef));
		} catch (IOException e) {
		    throw new JMSException(e.getLocalizedMessage());
		}
	    }
	});
    }

    private String getFeed(NodeRef nodeRef) throws IOException {
	String name = (String) serviceRegistry.getNodeService().getProperty(
		nodeRef, ContentModel.PROP_NAME);

	String url = UrlUtil.getShareUrl(serviceRegistry.getSysAdminParams())
		+ "/page/site/"
		+ serviceRegistry.getSiteService().getSite(nodeRef)
			.getShortName() + "/document-details?nodeRef="
		+ nodeRef.toString();

	Abdera abdera = Abdera.getInstance();
	Feed feed = abdera.newFeed();

	feed.setId(nodeRef.getId() + "/feed");
	feed.setTitle("Policy Approval");
	feed.setUpdated(new Date());
	feed.addAuthor(serviceRegistry.getAuthenticationService()
		.getCurrentUserName());

	Entry entry = feed.addEntry();
	entry.setId(nodeRef.getId() + "/entry");
	entry.setTitle(name);
	entry.setUpdated(new Date());
	entry.setPublished(new Date());
	entry.addLink(url);

	StringWriter w = new StringWriter();
	abdera.getWriterFactory().getWriter("prettyxml").writeTo(feed, w);

	return w.toString();
    }
}
