package com.tinderbot.telegram.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private String name;
    private String sex;
    private String age;
    private String city;
    private String occupation;
    private String hobby;
    private String handsome;
    private String wealth;
    private String annoys;
    private String goals;

    @Override
    public String toString() {
        return fieldToString(name, "Имя")
                + fieldToString(sex, "Пол")
                + fieldToString(age, "Возраст")
                + fieldToString(city, "Город")
                + fieldToString(occupation, "Профессия")
                + fieldToString(hobby, "Хобби")
                + fieldToString(handsome, "Красота/привлекательность (макс. 10)")
                + fieldToString(wealth, "Доход/богатство")
                + fieldToString(annoys, "В людях раздражает")
                + fieldToString(goals, "Цели знакомства");
    }

    private String fieldToString(String str, String description) {
        return (str != null && !str.isEmpty()) ? description + ": " + str + "\n" : "";
    }
}