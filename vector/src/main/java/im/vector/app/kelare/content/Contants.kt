package im.vector.app.kelare.content


import org.linphone.core.Account
import org.linphone.core.Core

object Contants {

    //const val HOME_SERVER = "https://kelare.istory.cc:8448/"
//    const val HOME_SERVER = "https://nodefy.happy-fish.cn:8448/"

//    const val PRIMARY_USER_ID = "@qijs:kelare.istory.cc"
//    const val ACCESS_TOKEN = "syt_cWlqcw_vMwfeaXIEwDzClIXCytL_0AbdEa"

    /*const val PRIMARY_USER_ID = "@fourier4:kelare.istory.cc"
    const val ACCESS_TOKEN = "syt_Zm91cmllcjQ_pMvKTTFjbnrmvWPRnJAD_3WuO6k"*/

    lateinit var mBus: AndroidBus
    lateinit var core: Core
    lateinit var account:Account

//    const val Sip_Domain = "119.28.64.168"
//    const val Proxy_Domain = "119.28.64.168"

    const val Sip_Domain = "comms.kelare-demo.com"
    const val Proxy_Domain = "comms-ext.kelare-demo.com"

    const val Account3_Pwd = "7cU3rjjJjb4EXqwFqTHBvLzAjy7A3s"
    const val Account2_Pwd = "P@55word1!"

    //gitter.im server
    const val SERVER_GITTER = "gitter.im"

    //机器人房间名字
    const val SkypeBotRoomName = "Skype-Bot-Room-GmQHk5QBe5RFVL"
    const val WhatsAppBotRoomName = "WhatsApp-Bot-Room-GmQHk5QBe5RFVL"
    const val TelegramBotRoomName = "Telegram-Bot-Room-GmQHk5QBe5RFVL"
    const val SlackBotRoomName = "Slack-Bot-Room-GmQHk5QBe5RFVL"

    //机器人ID
    const val SkypeBotID = "skype_bot_room_id"
    const val WhatsAppBotID = "whatsapp_bot_room_id"
    const val TelegramBotID = "telegram_bot_room_id"
    const val SlackBotID = "slack_bot_room_id"

    //机器人名字
    const val SkypeBotName = "@skypebridgebot"
    const val WhatsAppBotName = "@whatsappbot"
    const val TelegramBotName = "@telegrambot"
    const val SlackBotName = "@_slackpuppet_bot"

    //第三方账号用户前缀
    const val SkypeUserIDPrefix = "@skype-"
    const val WhatsAppUserIDPrefix = "@whatsapp_"
    const val TelegramUserIDPrefix = "@telegram_"
    const val SlackUserIDPrefix = "@_slackpuppet_"




}
