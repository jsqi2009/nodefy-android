package im.vector.app.kelare.network.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * author : Jason
 * date   : 3/25/22 11:28 AM
 * desc   :
 */
@Entity
public class PeopleMessage {

    @Property
    private String people_room_id;
    @Property
    private String login_account_jid;
    @Property
    private String login_name;
    @Property
    private String chat_with_jid;
    @Property
    private String message;
    @Property
    private Boolean isSend;
    @Property
    private long timestamp;
    @Generated(hash = 1860796378)
    public PeopleMessage(String people_room_id, String login_account_jid,
            String login_name, String chat_with_jid, String message, Boolean isSend,
            long timestamp) {
        this.people_room_id = people_room_id;
        this.login_account_jid = login_account_jid;
        this.login_name = login_name;
        this.chat_with_jid = chat_with_jid;
        this.message = message;
        this.isSend = isSend;
        this.timestamp = timestamp;
    }
    @Generated(hash = 524975984)
    public PeopleMessage() {
    }
    public String getPeople_room_id() {
        return this.people_room_id;
    }
    public void setPeople_room_id(String people_room_id) {
        this.people_room_id = people_room_id;
    }
    public String getChat_with_jid() {
        return this.chat_with_jid;
    }
    public void setChat_with_jid(String chat_with_jid) {
        this.chat_with_jid = chat_with_jid;
    }
    public String getMessage() {
        return this.message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public Boolean getIsSend() {
        return this.isSend;
    }
    public void setIsSend(Boolean isSend) {
        this.isSend = isSend;
    }
    public String getLogin_account_jid() {
        return this.login_account_jid;
    }
    public void setLogin_account_jid(String login_account_jid) {
        this.login_account_jid = login_account_jid;
    }
    public String getLogin_name() {
        return this.login_name;
    }
    public void setLogin_name(String login_name) {
        this.login_name = login_name;
    }
    public long getTimestamp() {
        return this.timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
