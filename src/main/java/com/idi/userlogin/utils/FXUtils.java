package com.idi.userlogin.utils;

import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Node;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

public class FXUtils {

    public static void fadeIn(Node node,Duration duration) {
        DoubleProperty opacity = node.opacityProperty();
        Timeline fadeIn = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(opacity, 0.0)),
                new KeyFrame(duration, new KeyValue(opacity, 1.0))
        );
        fadeIn.play();
    }


    public static void fadeOut(Node node,Duration duration) {
        PauseTransition pauseTransition = new PauseTransition(Duration.millis(2000)); // wait a second
        FadeTransition ft = new FadeTransition();
        ft.setNode(node);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setRate(2.0);
        ft.setDuration(duration);
        SequentialTransition s = new SequentialTransition(pauseTransition, ft);
        s.play();
    }
}
