package im.vector.app.kelare.greendao

import im.vector.app.kelare.network.models.GroupMessage
import im.vector.app.kelare.network.models.GroupRoom
import im.vector.app.kelare.network.models.PeopleMessage
import im.vector.app.kelare.network.models.PeopleRoom
import im.vector.app.kelare.network.models.SipMessage
import org.greenrobot.greendao.query.QueryBuilder

/**
 * author : Jason
 *  date   : 3/18/22 1:04 PM
 *  desc   :
 */
object DaoUtils {

    /**
     * insert sip message
     */
    fun insertSipMsg(daoSession: DaoSession, item: SipMessage) {
        val sipMessageDao = daoSession.sipMessageDao
        sipMessageDao.insert(item)
    }

    /**
     * query all sip message
     */
    fun queryAllSipMsg(daoSession: DaoSession): List<SipMessage> {

        val sipMessageDao = daoSession.sipMessageDao
        return sipMessageDao.loadAll()
    }

    /***
     * query sip message by name
     */
    fun querySipMsgByName(daoSession: DaoSession, value: String): List<SipMessage> {
        val sipMessageDao = daoSession.sipMessageDao
        val queryBuilder: QueryBuilder<SipMessage> = sipMessageDao.queryBuilder()
        val messageQueryBuilder: QueryBuilder<SipMessage> = queryBuilder.where(SipMessageDao.Properties.Chat_room_id.eq(value))
            .orderAsc(SipMessageDao.Properties.Chat_room_id)


        return messageQueryBuilder.list()
    }

    /***
     * delete sip message by ID
     */
    fun deleteSipMsgByID(daoSession: DaoSession, roomID: String){
        val sipMessageDao = daoSession.sipMessageDao
        val queryBuilder: QueryBuilder<SipMessage> = sipMessageDao.queryBuilder()
        val messageQueryBuilder: QueryBuilder<SipMessage> = queryBuilder.where(SipMessageDao.Properties.Chat_room_id.eq(roomID)).orderAsc(SipMessageDao.Properties.Chat_room_id)
       // val room  = queryBuilder.where(SipMessageDao.Properties.Chat_room_id.eq(roomID)).list()
        /*for (groupRoom in room) {
            sipMessageDao.delete(groupRoom)
        }*/

        for (sipMessage in messageQueryBuilder.list()) {
            sipMessageDao.delete(sipMessage)
        }
    }

    /**
     * insert people chat room
     */
    fun insertPeopleChatRoom(daoSession: DaoSession, item: PeopleRoom) {
        val peopleRoomDao = daoSession.peopleRoomDao
        peopleRoomDao.insert(item)
    }

    /**
     * query all people chat room
     */
    fun queryAllPeopleRoom(daoSession: DaoSession): List<PeopleRoom> {

        val peopleRoomDao = daoSession.peopleRoomDao
        return peopleRoomDao.loadAll()
    }

    /**
     * insert people chat message
     */
    fun insertPeopleChatMessage(daoSession: DaoSession, item: PeopleMessage) {
        val peopleMessageDao = daoSession.peopleMessageDao
        peopleMessageDao.insert(item)
    }

    /**
     * query people chat message
     */
    fun queryAllPeopleChatMessage(daoSession: DaoSession): List<PeopleMessage> {

        val peopleMessageDao = daoSession.peopleMessageDao
        return peopleMessageDao.loadAll()
    }

    /***
     * query sip message by RoomId
     */
    fun queryPeopleMsgById(daoSession: DaoSession, value: String): List<PeopleMessage> {
        val peopleMessageDao = daoSession.peopleMessageDao
        val queryBuilder: QueryBuilder<PeopleMessage> = peopleMessageDao.queryBuilder()
        val messageQueryBuilder: QueryBuilder<PeopleMessage> = queryBuilder.where(PeopleMessageDao.Properties.People_room_id.eq(value))
            .orderAsc(PeopleMessageDao.Properties.People_room_id)

        return messageQueryBuilder.list()
    }

    /**
     * insert group chat room
     */
    fun insertGroupChatRoom(daoSession: DaoSession, item: GroupRoom) {
        val groupRoomDao = daoSession.groupRoomDao
        groupRoomDao.insert(item)
    }

    /**
     * update group chat room
     */
    fun updateGroupChatRoom(daoSession: DaoSession, roomID: String, users: String) {

        val groupRoomDao = daoSession.groupRoomDao
        val queryBuilder = groupRoomDao.queryBuilder()
        val room  = queryBuilder.where(GroupRoomDao.Properties.Group_room_id.eq(roomID)).build().unique()
        room.participants = users

        groupRoomDao.update(room)
    }

    /**
     * delete group chat room
     */
    fun deleteGroupChatRoom(daoSession: DaoSession, roomID: String) {

        val groupRoomDao = daoSession.groupRoomDao
        val queryBuilder = groupRoomDao.queryBuilder()
        //val room  = queryBuilder.where(GroupRoomDao.Properties.Group_room_id.eq(roomID)).build().unique()
        val room  = queryBuilder.where(GroupRoomDao.Properties.Group_room_id.eq(roomID)).list()
        for (groupRoom in room) {
            groupRoomDao.delete(groupRoom)
        }
    }

    /**
     * query all group chat room
     */

    fun queryAllGroupRoom(daoSession: DaoSession): List<GroupRoom> {

        val groupRoomDao = daoSession.groupRoomDao
        return groupRoomDao.loadAll()
    }

    /**
     * insert group chat message
     */
    fun insertGroupChatMessage(daoSession: DaoSession, item: GroupMessage) {
        val groupMessageDao = daoSession.groupMessageDao
        groupMessageDao.insert(item)
    }

    /**
     * query group chat message
     */
    fun queryAllGroupChatMessage(daoSession: DaoSession): List<GroupMessage> {

        val groupMessageDao = daoSession.groupMessageDao
        return groupMessageDao.loadAll()
    }

    /***
     * query group message by RoomId
     */
    fun queryGroupMsgById(daoSession: DaoSession, value: String): List<GroupMessage> {
        val groupMessageDao = daoSession.groupMessageDao
        val queryBuilder: QueryBuilder<GroupMessage> = groupMessageDao.queryBuilder()
        val messageQueryBuilder: QueryBuilder<GroupMessage> = queryBuilder.where(GroupMessageDao.Properties.Group_room_id.eq(value))
            .orderAsc(GroupMessageDao.Properties.Group_room_id)

        return messageQueryBuilder.list()
    }




}
