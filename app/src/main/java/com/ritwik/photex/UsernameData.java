package com.ritwik.photex;

public class UsernameData {
    int imageResourceId = 0;
    private String platform = "";

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
        switch (platform)
        {
            case "Instagram":
                setImageResourceId(R.drawable.instagram);
                break;
            case "Facebook":
                setImageResourceId(R.drawable.facebook);
                break;
            case "Twitter":
                setImageResourceId(R.drawable.twitter);
                break;
            case "Youtube":
                setImageResourceId(R.drawable.youtube);
                break;

        }
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    private  void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }

    String username = "";



    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
