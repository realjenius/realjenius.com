package util;

import models.DataSet;
import models.PageSet;
import models.PostSet;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

@OnApplicationStart
public class PostLoader extends Job<Void> {
	@Override
	public void doJob() throws Exception {
		DataSet.reload();
	}
}	
