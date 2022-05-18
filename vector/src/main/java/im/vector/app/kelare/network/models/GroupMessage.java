package im.vector.app.kelare.network.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * author : Jason
 * date   : 4/19/22 5:53 PM
 * desc   :
 */

@Entity
public class GroupMessage {

    @Property
    private String group_room_id;
    @Property
    private String group_room_name;
    @Property
    private String message;
    @Property
    private String message_from;
    @Property
    private Long timestamp;
    @Property
    private boolean isRead;
    @Property
    private boolean isSend;
    @Generated(hash = 135270930)
    public GroupMessage(String group_room_id, String group_room_name,
            String message, String message_from, Long timestamp, boolean isRead,
            boolean isSend) {
        this.group_room_id = group_room_id;
        this.group_room_name = group_room_name;
        this.message = message;
        this.message_from = message_from;
        this.timestamp = timestamp;
        this.isRead = isRead;
        this.isSend = isSend;
    }
    @Generated(hash = 159954481)
    public GroupMessage() {
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
    public String getMessage() {
        return this.message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage_from() {
        return this.message_from;
    }
    public void setMessage_from(String message_from) {
        this.message_from = message_from;
    }
    public Long getTimestamp() {
        return this.timestamp;
    }
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    public boolean getIsRead() {
        return this.isRead;
    }
    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }
    public boolean getIsSend() {
        return this.isSend;
    }
    public void setIsSend(boolean isSend) {
        this.isSend = isSend;
    }
}
