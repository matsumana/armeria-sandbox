package info.matsumana.armeria.bean.handler;

import java.io.Serializable;

public class HelloResponse implements Serializable {

    private static final long serialVersionUID = -3828780691636842866L;

    private String serverName;
    private String message;

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "HelloResponse{" +
               "serverName='" + serverName + '\'' +
               ", message='" + message + '\'' +
               '}';
    }
}
