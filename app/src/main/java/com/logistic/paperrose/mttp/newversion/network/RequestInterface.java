package com.logistic.paperrose.mttp.newversion.network;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by paperrose on 30.06.2016.
 */
public interface RequestInterface {
    //GET|POST  user/auth
    @FormUrlEncoded
    @POST("user/auth")
    void userAuthPost(@Field("client_id") String client_id,
                  @Field("auth_token") String auth_token,
                  @Field("device_id") String device_id,
                  @Field("login") String login,
                  @Field("device_info") String device_info,
                  Callback<RequestBody> callback);
    @GET("user/auth")
    void userAuthGet(@Field("client_id") String client_id,
                  @Field("auth_token") String auth_token,
                  @Field("device_id") String device_id,
                  @Field("login") String login,
                  @Field("device_info") String device_info,
                  Callback<RequestBody> callback);


    //GET|POST  /user/exit
    @FormUrlEncoded
    @POST("user/exit")
    void userExitPost(@Field("access_token") String access_token,
                      @Field("login") String login,
                      @Field("device_info") String device_info,
                      Callback<RequestBody> callback);
    @GET("user/exit")
    void userExitGet(@Field("access_token") String access_token,
                     @Field("login") String login,
                     @Field("device_info") String device_info,
                     Callback<RequestBody> callback);


    //GET  /user/push
    @GET("user/push")
    void userPushGet(@Field("access_token") String access_token,
                     @Field("login") String login,
                     @Field("device_info") String device_info,
                     Callback<RequestBody> callback);


    //GET  /user/devices
    @GET("user/devices")
    void userDevicesGet(@Field("access_token") String access_token,
                        @Field("login") String login,
                        @Field("device_info") String device_info,
                        Callback<RequestBody> callback);

    //POST  /uploadDoc
    @FormUrlEncoded
    @POST("uploadDoc")
    void uploadDocPost(@Field("access_token") String access_token,
                       @Field("login") String login,
                       @Field("device_info") String device_info,
                       Callback<RequestBody> callback);


    //POST  /testUploadDoc

    //GET|POST  /user/available_tables
    @FormUrlEncoded
    @POST("user/available_tables")
    void userAvailableTablesPost(@Field("access_token") String access_token,
                                 @Field("login") String login,
                                 @Field("device_info") String device_info,
                                 Callback<RequestBody> callback);
    @GET("user/available_tables")
    void userAvailableTablesGet(@Field("access_token") String access_token,
                                @Field("login") String login,
                                @Field("device_info") String device_info,
                                Callback<RequestBody> callback);


    //GET|POST  /change_status
    @FormUrlEncoded
    @POST("change_status")
    void changeStatusPost(@Field("access_token") String access_token,
                          @Field("login") String login,
                          @Field("device_info") String device_info,
                          Callback<RequestBody> callback);
    @GET("change_status")
    void changeStatusGet(@Field("access_token") String access_token,
                         @Field("login") String login,
                         @Field("device_info") String device_info,
                         Callback<RequestBody> callback);


    //GET|POST  /search/dictionary_search
    @FormUrlEncoded
    @POST("search/dictionary_search")
    void searchDictionarySearchPost(@Field("access_token") String access_token,
                                    @Field("login") String login,
                                    @Field("device_info") String device_info,
                                    Callback<RequestBody> callback);
    @GET("user/available_tables")
    void userDictionarySearchGet(@Field("access_token") String access_token,
                                 @Field("login") String login,
                                 @Field("device_info") String device_info,
                                 Callback<RequestBody> callback);


    //GET|POST  /search/bookmark_search
    @FormUrlEncoded
    @POST("search/bookmark_search")
    void searchBookmarkSearchPost(@Field("access_token") String access_token,
                                  @Field("login") String login,
                                  @Field("device_info") String device_info,
                                  Callback<RequestBody> callback);
    @GET("search/bookmark_search")
    void searchBookmarkSearchGet(@Field("access_token") String access_token,
                                 @Field("login") String login,
                                 @Field("device_info") String device_info,
                                 Callback<RequestBody> callback);


    //GET|POST  /remove_bookmark
    @FormUrlEncoded
    @POST("remove_bookmark")
    void removeBookmarkPost(@Field("access_token") String access_token,
                            @Field("login") String login,
                            @Field("device_info") String device_info,
                            Callback<RequestBody> callback);
    @GET("remove_bookmark")
    void removeBookmarkGet(@Field("access_token") String access_token,
                           @Field("login") String login,
                           @Field("device_info") String device_info,
                           Callback<RequestBody> callback);


    //GET|POST  /user/month_refresh
    @FormUrlEncoded
    @POST("user/month_refresh")
    void userMonthRefreshPost(@Field("access_token") String access_token,
                              @Field("login") String login,
                              @Field("device_info") String device_info,
                              Callback<RequestBody> callback);
    @GET("user/month_refresh")
    void userMonthRefreshGet(@Field("access_token") String access_token,
                             @Field("login") String login,
                             @Field("device_info") String device_info,
                             Callback<RequestBody> callback);


    //GET|POST  /user/available_fields
    @FormUrlEncoded
    @POST("user/available_fields")
    Call<ResponseBody> userAvailableFieldsPost(@Field("access_token") String access_token,
                                           @Field("login") String login,
                                           @Field("device_info") String device_info);
    @GET("user/available_fields")
    Call<ResponseBody> userAvailableFieldsGet(@Field("access_token") String access_token,
                                              @Field("login") String login,
                                              @Field("device_info") String device_info);


    //GET|POST  /edit
    @FormUrlEncoded
    @POST("edit")
    void editPost(@Field("access_token") String access_token,
                  @Field("login") String login,
                  @Field("device_info") String device_info,
                  Callback<RequestBody> callback);
    @GET("edit")
    void editGet(@Field("access_token") String access_token,
                 @Field("login") String login,
                 @Field("device_info") String device_info,
                 Callback<RequestBody> callback);


    //GET|POST  /test

    //GET|POST  /get_nomenclatures
    @FormUrlEncoded
    @POST("get_nomenclatures")
    void getNomenclaturesPost(@Field("access_token") String access_token,
                              @Field("login") String login,
                              @Field("device_info") String device_info,
                              Callback<RequestBody> callback);
    @GET("get_nomenclatures")
    void getNomenclaturesGet(@Field("access_token") String access_token,
                             @Field("login") String login,
                             @Field("device_info") String device_info,
                             Callback<RequestBody> callback);


    //GET|POST  /user/pushes
    @FormUrlEncoded
    @POST("user/pushes")
    void userPushesPost(@Field("access_token") String access_token,
                        @Field("login") String login,
                        @Field("device_info") String device_info,
                        Callback<RequestBody> callback);
    @GET("user/pushes")
    void userPushesGet(@Field("access_token") String access_token,
                       @Field("login") String login,
                       @Field("device_info") String device_info,
                       Callback<RequestBody> callback);


    //GET|POST  /add_bookmark
    @FormUrlEncoded
    @POST("add_bookmark")
    void addBookmarkPost(@Field("access_token") String access_token,
                         @Field("login") String login,
                         @Field("device_info") String device_info,
                         Callback<RequestBody> callback);
    @GET("add_bookmark")
    void addBookmarkGet(@Field("access_token") String access_token,
                        @Field("login") String login,
                        @Field("device_info") String device_info,
                        Callback<RequestBody> callback);


    //GET|POST  /edit_bookmark
    @FormUrlEncoded
    @POST("edit_bookmark")
    void editBookmarkPost(@Field("access_token") String access_token,
                          @Field("login") String login,
                          @Field("device_info") String device_info,
                          Callback<RequestBody> callback);
    @GET("edit_bookmark")
    void editBookmarkGet(@Field("access_token") String access_token,
                         @Field("login") String login,
                         @Field("device_info") String device_info,
                         Callback<RequestBody> callback);


    //GET|POST  /edit_bookmark_order
    @FormUrlEncoded
    @POST("edit_bookmark_order")
    void editBookmarkOrderPost(@Field("access_token") String access_token,
                               @Field("login") String login,
                               @Field("device_info") String device_info,
                               Callback<RequestBody> callback);
    @GET("edit_bookmark_order")
    void editBookmarkOrderGet(@Field("access_token") String access_token,
                              @Field("login") String login,
                              @Field("device_info") String device_info,
                              Callback<RequestBody> callback);


    //GET|POST  /user/check_auth
    @FormUrlEncoded
    @POST("user/check_auth")
    void userCheckAuthPost(@Field("access_token") String access_token,
                           @Field("login") String login,
                           @Field("device_info") String device_info,
                           Callback<RequestBody> callback);
    @GET("user/check_auth")
    void userCheckAuthGet(@Field("access_token") String access_token,
                          @Field("login") String login,
                          @Field("device_info") String device_info,
                          Callback<RequestBody> callback);


    //GET|POST  /user/request_fields
    @FormUrlEncoded
    @POST("user/request_fields")
    void userRequestFieldsPost(@Field("access_token") String access_token,
                               @Field("login") String login,
                               @Field("device_info") String device_info,
                               Callback<RequestBody> callback);
    @GET("user/request_fields")
    void userRequestFieldsGet(@Field("access_token") String access_token,
                              @Field("login") String login,
                              @Field("device_info") String device_info,
                              Callback<RequestBody> callback);


    //GET|POST  /search
    @FormUrlEncoded
    @POST("search")
    void searchPost(@Field("access_token") String access_token,
                    @Field("login") String login,
                    @Field("device_info") String device_info,
                    Callback<RequestBody> callback);
    @GET("search")
    void searchGet(@Field("access_token") String access_token,
                   @Field("login") String login,
                   @Field("device_info") String device_info,
                   Callback<RequestBody> callback);


    //GET|POST  /search_by_num
    @FormUrlEncoded
    @POST("search_by_num")
    void searchByNumPost(@Field("access_token") String access_token,
                         @Field("login") String login,
                         @Field("device_info") String device_info,
                         Callback<RequestBody> callback);
    @GET("search_by_num")
    void searchByNumGet(@Field("access_token") String access_token,
                        @Field("login") String login,
                        @Field("device_info") String device_info,
                        Callback<RequestBody> callback);


    //GET|POST  /search/dict
    @FormUrlEncoded
    @POST("search/dict")
    void searchDictPost(@Field("access_token") String access_token,
                        @Field("login") String login,
                        @Field("device_info") String device_info,
                        Callback<RequestBody> callback);
    @GET("search/dict")
    void searchDictGet(@Field("access_token") String access_token,
                       @Field("login") String login,
                       @Field("device_info") String device_info,
                       Callback<RequestBody> callback);


    //GET|POST  /declarations
    @FormUrlEncoded
    @POST("declarations")
    void declarationsPost(@Field("access_token") String access_token,
                          @Field("login") String login,
                          @Field("device_info") String device_info,
                          Callback<RequestBody> callback);
    @GET("declarations")
    void declarationsGet(@Field("access_token") String access_token,
                         @Field("login") String login,
                         @Field("device_info") String device_info,
                         Callback<RequestBody> callback);


    //GET|POST  /declarations/group_statistic

    //GET|POST  /declarations/get_statistic

    //GET|POST  /declarations/edit
    @FormUrlEncoded
    @POST("declarations/edit")
    void declarationsEditPost(@Field("access_token") String access_token,
                              @Field("login") String login,
                              @Field("device_info") String device_info,
                              Callback<RequestBody> callback);
    @GET("declarations/edit")
    void declarationsEditGet(@Field("access_token") String access_token,
                             @Field("login") String login,
                             @Field("device_info") String device_info,
                             Callback<RequestBody> callback);


    //GET|POST  /search/autocomplete
    @FormUrlEncoded
    @POST("search/autocomplete")
    void searchAutocompletePost(@Field("access_token") String access_token,
                                @Field("login") String login,
                                @Field("device_info") String device_info,
                                Callback<RequestBody> callback);
    @GET("search/autocomplete")
    void searchAutocompleteGet(@Field("access_token") String access_token,
                               @Field("login") String login,
                               @Field("device_info") String device_info,
                               Callback<RequestBody> callback);


    //GET|POST  /clients

    //GET|POST  /partners

}
