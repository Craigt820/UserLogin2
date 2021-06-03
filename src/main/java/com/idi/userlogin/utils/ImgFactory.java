package com.idi.userlogin.utils;


import javafx.scene.image.ImageView;

public class ImgFactory {

    public enum IMGS {

        CHECKMARK(ImgFactory.class.getResource("/images/checkmark.png").toExternalForm()), EXMARK(ImgFactory.class.getResource("/images/exmark.png").toExternalForm());

        private String loc;

        IMGS(String loc) {
            this.loc = loc;
            }

        public String getLoc() {
            return loc;
        }

        public void setLoc(String loc) {
            this.loc = loc;
        }
    }

    private ImageView view;

    public static ImageView createView(IMGS imgs) {
        return new ImageView(imgs.loc);
    }
}
