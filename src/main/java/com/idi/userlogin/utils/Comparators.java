package com.idi.userlogin.utils;

import javafx.scene.control.ComboBox;
import org.controlsfx.control.CheckComboBox;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Comparators<T> {

    public static class ComboCompare<T> implements Comparator<ComboBox<T>> {

        @Override
        public int compare(ComboBox<T> o1, ComboBox<T> o2) {
            return o1.getSelectionModel().getSelectedItem().toString().compareTo(o2.getSelectionModel().getSelectedItem().toString());
        }

        @Override
        public boolean equals(Object obj) {
            return false;
        }
    }



    public static class CheckComboCompare<T> implements Comparator<CheckComboBox<String>> {

        @Override
        public int compare(CheckComboBox<String> o1, CheckComboBox<String> o2) {
            List<String> o1s = o1.getCheckModel().getCheckedItems().stream().sorted().filter(e -> !e.isEmpty()).collect(Collectors.toList());
            List<String> o2s = o2.getCheckModel().getCheckedItems().stream().sorted().filter(e -> !e.isEmpty()).collect(Collectors.toList());

            if (o1s.isEmpty() || o2s.isEmpty()) {
                return 1;
            }

            return IntStream.range(0, o1s.size()).allMatch(e -> o1s.get(e).compareTo(o2s.get(e)) > 0) ? 1 : 0;
        }

        @Override
        public boolean equals(Object obj) {
            return false;
        }

    }

}
