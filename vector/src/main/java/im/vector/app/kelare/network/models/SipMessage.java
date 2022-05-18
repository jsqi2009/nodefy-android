/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app.kelare.network.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

/**
 * author : Jason
 * date   : 2022/3/16 13:28
 * desc   :
 */

@Entity
public class SipMessage {
    
    @Id(autoincrement = true)
    private Long id;
    @Property
    private String chat_room_id;
    @Property
    private String received_username;
    @Property
    private String received_domain;
    @Property
    private String send_username;
    @Property
    private String send_domain;
    @Property
    private String message_text;
    @Property
    private Long timestamp;
    @Property
    private boolean isSend;
    @Generated(hash = 32695842)
    public SipMessage(Long id, String chat_room_id, String received_username,
            String received_domain, String send_username, String send_domain,
            String message_text, Long timestamp, boolean isSend) {
        this.id = id;
        this.chat_room_id = chat_room_id;
        this.received_username = received_username;
        this.received_domain = received_domain;
        this.send_username = send_username;
        this.send_domain = send_domain;
        this.message_text = message_text;
        this.timestamp = timestamp;
        this.isSend = isSend;
    }
    @Generated(hash = 1542275248)
    public SipMessage() {
    }
    public String getChat_room_id() {
        return this.chat_room_id;
    }
    public void setChat_room_id(String chat_room_id) {
        this.chat_room_id = chat_room_id;
    }
    public String getReceived_username() {
        return this.received_username;
    }
    public void setReceived_username(String received_username) {
        this.received_username = received_username;
    }
    public String getReceived_domain() {
        return this.received_domain;
    }
    public void setReceived_domain(String received_domain) {
        this.received_domain = received_domain;
    }
    public String getSend_username() {
        return this.send_username;
    }
    public void setSend_username(String send_username) {
        this.send_username = send_username;
    }
    public String getSend_domain() {
        return this.send_domain;
    }
    public void setSend_domain(String send_domain) {
        this.send_domain = send_domain;
    }
    public String getMessage_text() {
        return this.message_text;
    }
    public void setMessage_text(String message_text) {
        this.message_text = message_text;
    }
    public Long getTimestamp() {
        return this.timestamp;
    }
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    public boolean getIsSend() {
        return this.isSend;
    }
    public void setIsSend(boolean isSend) {
        this.isSend = isSend;
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }




}
