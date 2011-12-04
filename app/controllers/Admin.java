package controllers;

import models.DataSet;
import models.PageSet;
import models.PostSet;
import play.mvc.Controller;

public class Admin extends Controller {

	public static void reload() throws Exception {
        DataSet.reload();
        Application.index("html");
	}
}
