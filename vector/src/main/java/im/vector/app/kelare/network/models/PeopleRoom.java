package im.vector.app.kelare.network.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * author : Jason
 * date   : 3/29/22 5:16 PM
 * desc   :
 */
@Entity
public class PeopleRoom {

    @Property
    private String people_room_id;
    @Property
    private String login_account_jid;
    @Property
    private String login_name;
    @Property
    private String login_account;
    @Property
    private String chat_with_jid;
    @Property
    private String latest_message;
    @Generated(hash = 75417771)
    public PeopleRoom(String people_room_id, String login_account_jid,
            String login_name, String login_account, String chat_with_jid,
            String latest_message) {
        this.people_room_id = people_room_id;
        this.login_account_jid = login_account_jid;
        this.login_name = login_name;
        this.login_account = login_account;
        this.chat_with_jid = chat_with_jid;
        this.latest_message = latest_message;
    }
    @Generated(hash = 1219067432)
    public PeopleRoom() {
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
    public String getLatest_message() {
        return this.latest_message;
    }
    public void setLatest_message(String latest_message) {
        this.latest_message = latest_message;
    }
    public String getLogin_account() {
        return this.login_account;
    }
    public void setLogin_account(String login_account) {
        this.login_account = login_account;
    }
}
