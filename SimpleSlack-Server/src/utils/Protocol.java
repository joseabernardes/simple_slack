/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import org.json.simple.JSONObject;

public class Protocol {

    public static final String COMMAND = "command";
    public static final String DATA = "data";

    public static final String ERROR = "error";
    public static final String BAD_COMMAND = "bad_command";

    public final class Client {

        public static final String COMMAND = "command";
        public static final String DATA = "data";

        public final class Auth {

            public static final String LOGIN = "login";
            public static final String LOGOUT = "logout";
            public static final String REGIST = "regist";
            public static final String EXIT = "exit";
        }

        public final class Private {

            public static final String SEND_MSG = "send_private_msg";
            public static final String SEND_FILE = "send_private_file";
            public static final String RECEIVE_FILE = "receive_private_file";
            public static final String LIST_LOGGED_USERS = "list_logged_users";
        }

        public final class Group {

            public static final String SEND_MSG = "send_group_msg";
            public static final String SEND_FILE = "send_group_file";
            public static final String RECEIVE_FILE = "receive_group_file";
            public static final String ADD = "add_group";
            public static final String JOIN = "join_group";
            public static final String EDIT = "edi_tgroup";
            public static final String REMOVE = "rem_group";
            public static final String LEAVE = "leave_group";
            public static final String LIST_GROUPS = "list_groups";
            public static final String LIST_GROUP_MSGS = "list_group_msgs";
        }


    }

    public final class Server {

        public final class Auth {

            public static final String LOGIN_SUCCESS = "login_success";
            public static final String LOGIN_ERROR = "login_error";
            public static final String REGIST_SUCCESS = "regist_success";
            public static final String REGIST_ERROR = "regist_error";

            public static final String EXIT = "exit";

            public final class Error {

                public static final String USER_PASS = "username_or_password";
                public static final String PASS_MATCH = "password_match";
                public static final String USER_EXISTS = "user_already_exists";
                public static final String REGEX = "regex_error";

            }

        }

        public final class Private {

            //SUCCESS
            public static final String RECEIVE_MSG = "receive_private_msg";
            public static final String RECEIVE_FILE = Client.Private.RECEIVE_FILE;
            public static final String SEND_FILE = Client.Private.SEND_FILE;
            public static final String FILE_SENDED = "file_sended";
            public static final String LIST_LOGGED_USERS = Client.Private.LIST_LOGGED_USERS;
            //ERRORS 
            public static final String SEND_ERROR = "send_error";
            public static final String FILE_ERROR = "file_error";

            public final class Error {

                public static final String USER = "no_username";
                public static final String FILE = "file_not_exists";
            }
        }

        public final class Group {

            //SUCCESS
            public static final String RECEIVE_MSG = "receive_group_msg";
            public static final String RECEIVE_FILE = Client.Group.RECEIVE_FILE;
            public static final String SEND_FILE = Client.Group.SEND_FILE;
            public static final String SEND_MSG = Client.Group.SEND_MSG;
            public static final String FILE_SENDED = "file_sended";
            public static final String ADD_SUCCESS = "group_add_success";
            public static final String JOIN_SUCCESS = "group_join_success";
            public static final String EDIT_SUCCESS = "group_edit_success";
            public static final String LEAVE_SUCCESS = "group_leave_success";
            public static final String LIST_GROUPS = Client.Group.LIST_GROUP_MSGS;
            public static final String LIST_GROUP_MSGS = Client.Group.LIST_GROUP_MSGS;

            //ERRORS
            public static final String SEND_ERROR = "send_error";
            public static final String FILE_ERROR = "file_error";
            public static final String ADD_ERROR = "add_error";
            public static final String JOIN_ERROR = "join_error";
            public static final String EDIT_ERROR = "edit_error";
            public static final String REMOVE_ERROR = "remove_error";
            public static final String LEAVE_ERROR = "leave_error";
            public static final String LIST_MSGS_ERROR = "list_groups_msgs_error";

            public final class Error {

                public static final String GROUP_NOT_EXISTS = "group_not_exists";
                public static final String GROUP_EXISTS = "group_exists";
                public static final String FILE = "file_not_exists";
                public static final String GROUP_NOT_EMPTY = "group_not_empty";
                public static final String GROUP_NOT_JOINED = "group_not_joined";
            }
        }
    }

    public static String makeJSONResponse(String command, String data) {
        JSONObject object = new JSONObject();
        object.put(Protocol.COMMAND, command);
        object.put(Protocol.DATA, data);
        return object.toJSONString();
    }
}
