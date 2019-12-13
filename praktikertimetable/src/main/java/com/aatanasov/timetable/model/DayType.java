package com.aatanasov.timetable.model;

/**
 * Type of a day, either working or not.
 */
public enum DayType {

    HOLIDAY("О"),
    SICK_LEAVE("Б"),
    REST("П"),
    EDUCATION("ОБ"),

    FIRST_SHIFT("1"),
    SECOND_SHIFT("2"),
    NORMAL_SHIFT("Р");



    String code;

    DayType(String codeArg) {
        code = codeArg;
    }

    public String getCode() {
        return code;
    }

    public static DayType fromString(String stringCellValue) {
        for (DayType type : DayType.values()) {
            if (type.code.equalsIgnoreCase(stringCellValue)) {
                return type;
            }
        }
        return null;
    }

    public static boolean isWorkingDay(DayType day) {
        switch (day) {
            case FIRST_SHIFT:
            case SECOND_SHIFT:
            case NORMAL_SHIFT:
                return true;

            default:
                return false;
        }
    }
}
