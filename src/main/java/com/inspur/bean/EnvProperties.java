package com.inspur.bean;

public class EnvProperties {

    private String nifiUrlPrefix = "";
    private final String nifiUsername = "nifiadmin";
    private final String nifiPasswd = "175d6b94-8dd9-40e9-ac7b-5df7dd55e8d9";

    private String authHost = "";
    private String authUser = "";
    private String authPasswd = "";

    private String realm = "";
    private String cluster = "";

    public String getNifiUrlPrefix() {
        return nifiUrlPrefix;
    }

    public void setNifiUrlPrefix(String nifiUrlPrefix) {
        this.nifiUrlPrefix = nifiUrlPrefix;
    }

    public String getNifiUsername() {
        return nifiUsername;
    }

    public String getNifiPasswd() {
        return nifiPasswd;
    }

    public String getAuthHost() {
        return authHost;
    }

    public void setAuthHost(String authHost) {
        this.authHost = authHost;
    }

    public String getAuthUser() {
        return authUser;
    }

    public void setAuthUser(String authUser) {
        this.authUser = authUser;
    }

    public String getAuthPasswd() {
        return authPasswd;
    }

    public void setAuthPasswd(String authPasswd) {
        this.authPasswd = authPasswd;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }
}
