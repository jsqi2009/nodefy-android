package im.vector.app.kelare.network.models;

import java.io.Serializable;

/**
 * author : Jason
 * date   : 3/23/22 5:54 PM
 * desc   :
 */
public class XmppFriends implements Serializable {
    private String jid;
    private String name;

    public XmppFriends() {
    }

    public XmppFriends(String jid, String name) {
        this.jid = jid;
        this.name = name;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
