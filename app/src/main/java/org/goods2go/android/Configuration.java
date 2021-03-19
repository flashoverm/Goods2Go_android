package org.goods2go.android;

public class Configuration {

    /*
     *   Network and messaging
     */
    //private static final String BACKEND_ADDRESS = "la.thral.de:8080";
    private static final String BACKEND_ADDRESS = "193.175.215.246:31100";
    //private static final String BACKEND_ADDRESS = "192.168.11.11:8080";

    public static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String URL_PREFIX_HTTP = "http://";
    private static final String URL_PREFIX_WS = "ws://";
    public static final String HTTP_BACKEND_ADDRESS = URL_PREFIX_HTTP+BACKEND_ADDRESS;
    private static final String WS_BACKEND_ADDRESS = URL_PREFIX_WS+BACKEND_ADDRESS;

    public static final String STOMP_ADDRESS = WS_BACKEND_ADDRESS + "/notifications";
    public static final String STOMP_TOPIC = "/user/queue/notification";

    public static final int CONNECTION_TIMEOUT = 5000;
    public static final int READ_TIMEOUT = 5000;

    /*
     *   QR
     */

    public static final int QR_SIZE = 300;

    /*
     *  Address view
     */

    public static final boolean SHOW_COUNTRY = false;

}
