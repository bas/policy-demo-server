function main()
{
	logger.log("Executing approve script");
    
    // set status to approved
	var source = bpm_package.children[0];
	
	logger.log("source: " + source.name);
	
	if (source.hasAspect("pld:policy"))
	{
		var manager = bpm_assignee;
		logger.log("manager: " + manager);

		for each (targetNode in source.assocs["pld:manager"])
		{
		   source.removeAssociation(targetNode, "pld:manager"); 
		}
		source.createAssociation(manager, "pld:manager");
		
		source.properties["pld:status"] = "Approved";
		var review = new Date();
		review.setFullYear(review.getFullYear() + 1);
		review.setMonth(review.getMonth() - 1);
		source.properties["pld:review"] = review;
		source.save();
	}
	
	logger.log("Setting effectivity...");
	
	if (source.hasAspect("cm:effectivity"))
	{
		var from = new Date();
		var to = new Date();
		to.setFullYear(to.getFullYear() + 1);
		source.properties["cm:from"] = from;
		source.properties["cm:to"] = to;
		source.save();
	}
	
	logger.log("Done...");

}

main();


