package models;

import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
@Entity
public class AdvertiserWishList extends Model {
    @Id
    private Integer wishID;
    private String advertiserEmail;
    private Integer eventID;

    public WishList(String advertiserEmail, Integer eventID){
        this.advertiserEmail = advertiserEmail;
        this.eventID = eventID;
    }

    public String getAdvertiserEmail() {
        return advertiserEmail;
    }

    public void setAdvertiserEmail(String advertiserEmail) {
        this.advertiserEmail = advertiserEmail;
    }

    public Integer getEventID() {
        return eventID;
    }

    public void setEventID(Integer eventID) {
        this.eventID = eventID;
    }
}