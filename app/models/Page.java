package models;

import org.joda.time.DateTime;
import play.mvc.Router;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class Page extends Content {
    public DateTime updated;

    public String absoluteUrl() {
        Router.ActionDefinition def = defn();
        def.absolute();
        return def.url;
    }

    private Router.ActionDefinition defn() {
        Map<String,Object> actionArgs = new HashMap<String,Object>();
        actionArgs.put("name", name);
        return Router.reverse("Application.page", actionArgs);
    }
}
