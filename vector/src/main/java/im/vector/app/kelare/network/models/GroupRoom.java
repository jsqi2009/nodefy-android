package im.vector.app.kelare.network.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

import java.io.Serializable;

/**
 * author : Jason
 * date   : 3/31/22 1:04 PM
 * desc   :
 */

@Entity
public class GroupRoom{

    @Id(autoincrement = true)
    private Long id;
    @Property
    private String group_room_id;
    @Property
    private String group_room_name;
    @Property
    private String latest_message;
    @Property
    private String message_from;
    @Property
    private String room_owner;
    @Property
    private String participants;
    @Generated(hash = 1430871746)
    public GroupRoom(Long id, String group_room_id, String group_room_name,
            String latest_message, String message_from, String room_owner,
            String participants) {
        this.id = id;
        this.group_room_id = group_room_id;
        this.group_room_name = group_room_name;
        this.latest_message = latest_message;
        this.message_from = message_from;
        this.room_owner = room_owner;
        this.participants = participants;
    }
    @Generated(hash = 499835189)
    public GroupRoom() {
    }
    public String getGroup_room_id() {
        return this.group_room_id;
    }
    public void setGroup_room_id(String group_room_id) {
        this.group_room_id = group_room_id;
    }
    public String getGroup_room_name() {
        return this.group_room_name;
    }
    public void setGroup_room_name(String group_room_name) {
        this.group_room_name = group_room_name;
    }
    public String getLatest_message() {
        return this.latest_message;
    }
    public void setLatest_message(String latest_message) {
        this.latest_message = latest_message;
    }
    public String getRoom_owner() {
        return this.room_owner;
    }
    public void setRoom_owner(String room_owner) {
        this.room_owner = room_owner;
    }
    public String getParticipants() {
        return this.participants;
    }
    public void setParticipants(String participants) {
        this.participants = participants;
    }
    public String getMessage_from() {
        return this.message_from;
    }
    public void setMessage_from(String message_from) {
        this.message_from = message_from;
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }

}
