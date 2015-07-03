package com.alfresco.demo.model;

import org.alfresco.service.namespace.QName;

public class PolicyModel {

	public static String POLICY_URI = "http://www.alfresco.com/model/demo/policy/1.0";
	public static String POLICY_PREFIX = "policy";

	public static QName ASPECT_POLICY = QName.createQName(POLICY_URI, "policy");
	public static QName PROP_POLICY_STATUS = QName.createQName(POLICY_URI, "status");

}
