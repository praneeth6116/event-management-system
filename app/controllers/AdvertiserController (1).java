package controllers;

import com.google.common.base.Ticker;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import models.*;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.api.libs.mailer.MailerClient;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.Advertiser.*;

import notifiers.MailerService;

import javax.inject.Inject;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import play.api.Logger;

public class AdvertiserController extends Controller{
	
	@Inject
	FormFactory formFactory;
	@Inject
	MailerClient mailerClient;
	
	public Result createAdvertiser() {
		return ok(createAdvertiser.render());
	}
	
	public Result saveAdvertiser(){
		DynamicForm df = formFactory.form().bindFromRequest();
		Advertiser advertiser = new Advertiser(df.get("customerFirstName"), df.get("customerLastName"), df.get("customerEmail"), df.get("customerPassword"), BigInteger.valueOf(Long.parseLong(df.get("customerPhoneNo"))));
		advertiser.save();
		MailerService m = new MailerService(mailerClient);
		m.verifyUser(advertiser);
		return redirect(routes.AdvertiserController.showAdvertiserDashBoard(advertiser.getUserEmail()));
	}
	
	public Result showAdvertiserDashBoard(String advertiserEmail) {
		User advertiser = User.find.byId(advertiserEmail);
		List<Event> allEvents = Ebean.find(Event.class).where().findList();
		List<EventManager> allEventManager = new ArrayList<>();
		for(Event e: allEvents) {
			EventManager em = EventManager.find.byId(e.getEventOwnerEmail());
			allEventManager.add(em);
		}
		return ok(showAdvertiserDashboard.render(advertiser,allEvents,allEventManager));
	}
	
	public Result showAdvertiserProfile(String advertiserEmail) {
		User advertiser = User.find.byId(advertiserEmail);
		return ok(showAdvertiserProfile.render(advertiser));
	}
	
	public Result addToAdvertiserWishList(Integer eventId){
		String user = session("connected");
		User advertiser = User.find.byId(user);
		AdvertiserWishList advertiserwishList = new AdvertiserWishList(advertiser.getUserEmail(),eventId);
		Event e = Event.find.byId(new Integer(eventId).toString());
		e.addObserver(advertiser.getUserEmail());
		List<Event> allEvents= Ebean.find(Event.class).where().findList();
		advertiserwishList.save();
		List<EventManager> allEventManager = new ArrayList<>();
		for(Event es: allEvents) {
			EventManager em = EventManager.find.byId(es.getEventOwnerEmail());
			allEventManager.add(em);
		}
		return ok(showAdvertiserDashboard.render(advertiser, allEvents,allEventManager));
	}
	
	public Result removeFromAdvertiserWishList(Integer eventId) {
		String user = session("connected");
		User advertiser = User.find.byId(user);
		AdvertiserWishList advertiserwishList = Ebean.find(AdvertiserWishList.class).where().eq("event_id", eventId).where().eq("advertiserEmail", advertiser.getUserEmail()).findUnique();
		advertiserwishList.delete();
		Event e = Event.find.byId(new Integer(eventId).toString());
		e.removeObserver(advertiser.getUserEmail());
		return redirect(routes.AdvertiserController.showAdvertiserWishList(user));
	}
	
	public Result showAdvertisedHistory(String advertiserEmail) {
		User user = User.find.byId(advertiserEmail);
		List<Share> Shares = Ebean.find(Share.class).where().eq("advertiserMail", advertiserEmail).findList();
		List<Event> listOfEvents = new ArrayList<>();
		if (Shares.size() > 0) {
			Iterator<Share> iter = Shares.iterator();
			while(iter.hasNext()) {
				Share s = iter.next();
				Event e = Event.find.byId(Integer.toString(s.getEventId()));
				listOfEvents.add(e);
			}
			String message = new String();
			return ok(showAdvertisedHistory.render(Shares, user, message,listOfEvents));
		}
		else {
			String message = new String("No Advertising History");
			return ok(showAdvertisedHistory.render(shares, user, message,listOfEvents));
		}
	}
	
	public Result showAdvertiserWishList(String advertiserEmail) {
		User advertiser = User.find.byId(advertiserEmail);
		List<AdvertiserWishList> advertiserwishList = Ebean.find(AdvertiserWishList.class).where().eq("advertiserEmail", advertiserEmail).findList();
		Iterator<AdvertiserWishList> iter = advertiserwishList.iterator();
		List<Event> eventWishList = new ArrayList<>();
		while(iter.hasNext()) {
			eventWishList.add(Event.find.byId(iter.next().getEventID().toString()));
		}
		return ok(showAdvertiserWishList.render(eventWishList, advertiser));
	}
	
	public Result updateAdvertiserProfile(String advertiserEmail) {
		User advertiser = Advertiser.find.byId(advertiserEmail);
		//List<Event> allEvents = Ebean.find(Event.class).where().findList();
		return ok(updateAdvertiserProfile.render(advertiser));
	}
	
	public Result modifyAdvertiserProfile(String advertiserEmail) {
		User advertiser = User.find.byId(advertiserEmail);
		DynamicForm df = formFactory.form().bindFromRequest();
		advertiser.setUserFirstName(df.get("customerFirstName"));
		advertiser.setUserLastName(df.get("customerLastName"));
		List<Event> allEvents = Ebean.find(Event.class).where().findList();
		advertiser.setPhoneNo(BigInteger.valueOf(Long.parseLong(df.get("customerPhoneNo"))));
		advertiser.update();
		List<EventManager> allEventManager = new ArrayList<>();
		for(Event e: allEvents) {
			EventManager em = EventManager.find.byId(e.getEventOwnerEmail());
			allEventManager.add(em);
		}
		return ok(showAdvertiserDashboard.render(advertiser,allEvents,allEventManager));
	}
	
	public Result searchEvent(String advertiserEmail) {
		DynamicForm df = formFactory.form().bindFromRequest();
		String name = df.get("query_name");
		String location = df.get("query_location");
		String date = df.get("query_date");
		User advertiser = User.find.byId(advertiserEmail);
		String  eventDate = new String();
		SimpleDateFormat datef = new SimpleDateFormat("MM/dd/yyyy");
		try {
			Date d = datef.parse(date);
			datef.applyPattern("yyyy-MM-dd HH:mm:ss.ssss");
			eventDate = datef.format(d);
		}
		catch(ParseException e) {
			e.printStackTrace();
		}
		Search search = new DateSearchDecorator(new LocationSearchDecorator(new EventSearchDecorator(new SimpleSearch(), name), location),eventDate);
		String sql = search.generateQuery();
		RawSql rawSql = RawSqlBuilder.parse(sql).columnMapping("event_id","eventId").create();
		Query<Event> query1 = Ebean.find(Event.class);
		query1.setRawSql(rawSql);
		List<Event> list = query1.findList();
		List<EventManager> allEventManager = new ArrayList<>();
		for(Event e: list) {
			EventManager em = EventManager.find.byId(e.getEventOwnerEmail());
			allEventManager.add(em);
		}
		return ok(showAdvertiserDashboard.render(advertiser,list,allEventManager));
	}
	
	/*
	public Result sendMail(String mail) {
		MailerService m = new MailerService(mailerClient);
		int status = m.sendEmail(mailerClient);
		return forbidden("Mailer service status:"+status);
	}
	*/
}