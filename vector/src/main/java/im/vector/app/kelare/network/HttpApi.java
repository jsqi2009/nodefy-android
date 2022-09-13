package im.vector.app.kelare.network;

import com.google.gson.JsonObject;

import im.vector.app.kelare.network.models.DefaultContactRelationInfo;
import im.vector.app.kelare.network.models.DeleteAccountInfo;
import im.vector.app.kelare.network.models.DeleteDialerContact;
import im.vector.app.kelare.network.models.DialerContactInfo;
import im.vector.app.kelare.network.models.SaveAccountInfo;
import im.vector.app.kelare.network.models.SetBotRoomInfo;
import im.vector.app.kelare.network.models.UpdateAccountInfo;

import java.util.Map;

import im.vector.app.kelare.network.models.UpdateContactRelationInfo;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;


public interface HttpApi {

    @POST("{key}/{key1}/{key2}/")
    @FormUrlEncoded
    Call<JsonObject> request(@Path("key") String key, @Path("key1") String key1, @Path("key2") String key2, @FieldMap Map<String, Object> map);

    @GET("{key}/{key1}/{key2}/")
    Call<JsonObject> requestAuth2(@HeaderMap Map<String, String> map, @Path("key") String key, @Path("key1") String key1, @Path("key2") String key2);

    @GET("{key}/{key1}/{key2}/{key3}/")
    Call<JsonObject> requestAuth3(@HeaderMap Map<String, String> map, @Path("key") String key, @Path("key1") String key1, @Path("key2") String key2, @Path("key3") String key3);


    @GET("{key}/{key1}/{key2}/")
    Call<JsonObject> requestAuth(@Header("Authorization") String authorization, @Path("key") String key, @Path("key1") String key1, @Path("key2") String key2);

    @GET("{key}/{key1}/{key2}/")
    Call<JsonObject> requestAuth(@HeaderMap Map<String, String> headerMap, @Path("key") String key, @Path("key1") String key1, @Path("key2") String key2, @QueryMap Map<String, Object> map);

    @PUT("{key}/{key1}/{key2}/")
    @FormUrlEncoded
    Call<JsonObject> requestAuthPut(@HeaderMap Map<String, String> headerMap, @Path("key") String key, @Path("key1") String key1, @Path("key2") String key2, @FieldMap Map<String, Object> map);

    @POST("{key}/{key1}/{key2}/")
    @FormUrlEncoded
    Call<JsonObject> requestAuthPost(@HeaderMap Map<String, String> headerMap, @Path("key") String key, @Path("key1") String key1, @Path("key2") String key2, @FieldMap Map<String, Object> map);





    @POST("{key1}/index.php")
    @FormUrlEncoded
    public Call<JsonObject> requestAuth(@Header("Authorization") String authorization, @Path("key1") String key1, @FieldMap Map<String, Object> map);

    @POST("/{key1}/{key2}/index.php")
    @FormUrlEncoded
    public Call<JsonObject> requestAuthWithSession(@Header("Authorization") String authorization, @Header("Sesssion-Id") String sessionId, @Path("key1") String key1, @Path("key2") String key2, @FieldMap Map<String, Object> map);

    @POST("/{key1}/{key2}/index.php")
    @Multipart
    public Call<JsonObject> uploadImage(@Header("Authorization") String authorization, @Path("key1") String key1, @Path("key2") String key2, @PartMap Map<String, RequestBody> params);

    @GET("/{key}/{key1}/index.php")
    public Call<JsonObject> requestAuthGet(@Header("Authorization") String authorization, @Path("key") String key,
                                           @Path("key1") String key1, @QueryMap Map<String, Object> map);

    @GET("_matrix/client/r0/sip_accounts")
    Call<JsonObject> getDialerAccount(@Header("Authorization") String authorization,  @QueryMap Map<String, Object> map);

    @GET("_matrix/client/r0/sip_accounts")
    Call<JsonObject> getDialerAccount2(@HeaderMap Map<String, String> headerMap, @QueryMap Map<String, Object> map);

    @POST("_matrix/client/r0/sip_accounts")
    Call<JsonObject> saveDialerAccount(@HeaderMap Map<String, String> headerMap, @Body SaveAccountInfo info);

    @PUT("_matrix/client/r0/sip_accounts")
    Call<JsonObject> updateDialerAccount(@HeaderMap Map<String, String> headerMap, @Body UpdateAccountInfo info);

    @HTTP(method = "DELETE", path = "_matrix/client/r0/sip_accounts", hasBody = true)
    Call<JsonObject> deleteDialerAccount(@HeaderMap Map<String, String> headerMap, @Body DeleteAccountInfo info);

    @GET("_matrix/client/r0/dialer_contacts")
    Call<JsonObject> getDialerContact(@HeaderMap Map<String, String> headerMap, @QueryMap Map<String, Object> map);

    @POST("_matrix/client/r0/dialer_contacts")
    Call<JsonObject> saveDialerContact(@HeaderMap Map<String, String> headerMap, @Body DialerContactInfo info);

    @PUT("_matrix/client/r0/dialer_contacts")
    Call<JsonObject> updateDialerContact(@HeaderMap Map<String, String> headerMap, @Body DialerContactInfo info);

    @HTTP(method = "DELETE", path = "_matrix/client/r0/dialer_contacts", hasBody = true)
    Call<JsonObject> deleteDialerContact(@HeaderMap Map<String, String> headerMap, @Body DeleteDialerContact info);

    @GET("_matrix/client/r0/public_room")
    Call<JsonObject> getPublicRoomInfo();

    @GET("_synapse/admin/v1/license_admin")
    Call<JsonObject> getLicense();

    @GET("_synapse/admin/v2/themes")
    Call<JsonObject> getThemes();

    @GET("_matrix/client/r0/account/account_contacts")
    Call<JsonObject> getAccountContact(@HeaderMap Map<String, String> headerMap);

    @GET("_matrix/client/r0/presence/{key}/status")
    Call<JsonObject> getPresenceStatus(@HeaderMap Map<String, String> headerMap, @Path("key") String key);

    @GET("_matrix/client/r0/relations")
    Call<JsonObject> getContactRelations(@HeaderMap Map<String, String> headerMap, @QueryMap Map<String, Object> map);

    @POST("_matrix/client/r0/relations")
    Call<JsonObject> updateContactRelations(@HeaderMap Map<String, String> headerMap, @Body UpdateContactRelationInfo info);

    @PUT("_matrix/client/r0/relations")
    Call<JsonObject> setContactDefaultChannel(@HeaderMap Map<String, String> headerMap, @Body DefaultContactRelationInfo info);

    @HTTP(method = "DELETE", path = "_matrix/client/r0/relations", hasBody = true)
    Call<JsonObject> deleteContactRelation(@HeaderMap Map<String, String> headerMap, @Body UpdateContactRelationInfo info);

    @GET("_matrix/client/r0/relations")
    Call<JsonObject> getAllContactRelations(@HeaderMap Map<String, String> headerMap);

    @GET("_matrix/client/r0/account/bot_accounts")
    Call<JsonObject> getBotAccounts(@HeaderMap Map<String, String> headerMap, @QueryMap Map<String, Object> map);

    @POST("_matrix/client/r0/account/bot_accounts")
    Call<JsonObject> setBridgeBotRoom(@HeaderMap Map<String, String> headerMap, @Body SetBotRoomInfo info);


}
