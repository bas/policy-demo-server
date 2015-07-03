function main() {
	if (document.hasAspect("cm:workingcopy") == false) {
		var workflow = actions.create("start-workflow");
		workflow.parameters.workflowName = "activiti$policyWorkflow";
		workflow.parameters["bpm:workflowDescription"] = "Please review and approve "
				+ document.name;
		workflow.parameters["bpm:assignee"] = people.getPerson("abeecher");
		workflow.parameters["policywf:legal"] = people.getPerson("mjackson");
		workflow.parameters["policywf:declareAsRecord"] = true;
		var futureDate = new Date();
		futureDate.setDate(futureDate.getDate() + 7);
		workflow.parameters["bpm:workflowDueDate"] = futureDate;
		return workflow.execute(document);
	}
}

main();
