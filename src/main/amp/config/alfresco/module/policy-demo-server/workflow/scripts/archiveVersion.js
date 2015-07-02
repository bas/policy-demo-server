function main() {
	logger.log("Executing declare as record script");

	// declare as record
    var declareAsRecord = execution.getVariable('pldwf_declareAsRecord');
    
    logger.log("Declare as record: " + declareAsRecord);
    
	if (declareAsRecord == true && siteService.hasSite("rm")) {
		var doc = bpm_package.children[0];
		var workingCopy = doc.checkout();
		doc = workingCopy.checkin("Approved Policy Document", true);
		var action = actions.create("declare-as-version-record");
		action.execute(doc);
	}
}

main();
