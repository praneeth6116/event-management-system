package controllers;

import akka.http.impl.engine.ws.WebSocket;
import io.ebean.Ebean;
import models.Advertiser;
import models.Event;
import models.Share;
import models.User;
import notifiers.MailerService;
import play.api.libs.mailer.MailerClient;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.filters.headers.SecurityHeadersFilter;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.Advertiser.*;
import views.html.EventManager.showEventManagerProfile;
import views.html.Share.*;
//import views.html.Share.bookShareDetails;
import views.html.Share.createShare;

import javax.inject.Inject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static java.lang.Boolean.TRUE;

public class ShareController extends Controller{

    @Inject
    FormFactory formFactory;
    @Inject
    MailerClient mailerClient;

    public Result bookShare(int eventId)
    {
        Event event = Event.find.byId(new Integer(eventId).toString());
        String mail = session("connected");
        User user = User.find.byId(mail);
        if(event.getAvailableNoOfShares() > 0)
        {
            return ok(createShare.render(event, user));
        }

        return forbidden(""+event.getEventName()+" is full");
    }

    public Result confirmShare(int eventId)
    {
        DynamicForm form = formFactory.form().bindFromRequest();
        String mail = session("connected");
        User user = User.find.byId(mail);
        Event event = Event.find.byId(new Integer(eventId).toString());

        EventController econ = new EventController();

        //return forbidden(""+mail+" "+event.getEventName());

        String eventManager = event.getEventOwnerEmail();

        Share s;
        try
        {
            Share temp = new Share(form.get("numshares"),eventManager,event.getEventId(),user.getUserEmail(),TRUE);
            s = temp;
        }
        catch (Exception e)
        {
            return forbidden("Error in share creation "+e);

        }

        s.save();
        MailerService m = new MailerService(mailerClient);
        m.bookingConfirmation(s,event);
        int status = econ.updateEvent(s, event.getEventId());
        //Event temp = Event.find.byId(new Integer(eventId).toString());
        //return forbidden("attendees: "+status);
        return ok(bookingSuccess.render(s,event,user));

        //return TODO;
    }

    public Result cancelShare(Integer shareId)
    {
        Share s = Share.find.byId(shareId.toString());
        String userId = session("connected");
        User user = User.find.byId(userId);
        Event event = Event.find.byId(new Integer(s.getEventId()).toString());

        EventController econ = new EventController();
        Integer status = econ.cancelEventShare(s,event);
        s.delete();
        MailerService m = new MailerService(mailerClient);
        m.cancelConfirmation(event,user,shareId);
        if (status == 0)
            return ok(cancelSuccess.render(user,event));
        else
            //return forbidden("Error in cancelling share "+status+" "+event.getEventId()+" "+event.getAttendees().size());
            return forbidden("Error in cancelling share "+status);

    }


}
