package kr.co.softcampus.how_do_i;

public class LoginUser {
    private String name;
    private String profile;
    private String email;
    private String id;

    public LoginUser(){}
    public LoginUser(String name, String profile, String email, String id) {
        this.name = name;
        this.profile = profile;
        this.email = email;
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
