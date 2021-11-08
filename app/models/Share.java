package models;

import com.sun.org.apache.xpath.internal.operations.Bool;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebeaninternal.server.lib.util.Str;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.security.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

@Entity
public class Share extends Model {

    @Id
    private int shareId;
    private int numShares;
    //int eventID;

    private String eventManagerMail;
    private int eventId;
    private String advertiserMail;
    private Date bookDate;
    private boolean Status;



    public Share(String shares, String eventManagerMail, int eventID, String advertiserMail, boolean status) throws Exception
    {
        this.numShares = new Integer(shares);
        this.eventManagerMail = eventManagerMail;

        this.eventId = eventID;
        this.advertiserMail = advertiserMail;
        String timeStamp = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
        SimpleDateFormat datef = new SimpleDateFormat("MM/dd/yyyy");
        Date cd = datef.parse(timeStamp);
        this.bookDate = cd;
        this.Status = status;

    }

    public static Finder<String, Share> find = new Finder<>(Share.class);

    public int getShareId() {
        return shareId;
    }

    public void setShareId(int shareId) {
        this.shareId = shareId;
    }

    public int getNumShares() {
        return numShares;
    }

    public void setNumShares(int numShares) {
        this.numShares = numShares;
    }

    public String getEventManagerMail() {
        return eventManagerMail;
    }

    public void setEventManagerMail(String eventManagerMail) {
        this.eventManagerMail = eventManagerMail;
    }

    public String getAdvertiserMail() {
        return advertiserMail;
    }

    public void setAdvertiserMail(String advertiserMail) {
        this.advertiserMail = advertiserMail;
    }

    public Date getBookDate() {
        return bookDate;
    }

    public void setBookDate(Date bookDate) {
        this.bookDate = bookDate;
    }

    public boolean isStatus() {
        return Status;
    }

    public void setStatus(boolean status) {
        Status = status;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventID) {
        this.eventId = eventID;
    }
}