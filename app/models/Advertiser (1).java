package models;

import controllers.AdvertiserController;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.ArrayList;

@Entity
@DiscriminatorValue("A")
@Table(name = "Advertiser")

public class Advertiser extends User implements EventObserver{
	
	private ArrayList<Event> advertiserwishList;
	private ArrayList<Event> advertisedEvents;
	private boolean hasAny;
	
	public Advertiser(String userFirstName, String userLastName, String userEmail, String userPassword, BigInteger phoneNo) {
		super(userFirstName, userLastName, userEmail, userPassword, phoneNo);
	}
	
	public ArrayList<Event> getSponsoredEvents() {
		return this.advertisedEvents;
	}
	
	/*
	public void updateUser() {
		AdvertiserController ccon = new AdvertiserController();
		try {
			FileWriter fw=new FileWriter("advertiser_updateuser.txt");
			fw.write("Welcome to javaTpoint. "+this.getUserEmail()+" "+this.getUserFirstName());
			fw.flush();
			fw.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		ccon.sendMail(this.getUserEmail());
	}
	*/
}