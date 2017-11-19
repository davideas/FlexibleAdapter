package eu.davidea.samples.flexibleadapter.persistence.db;

import android.arch.persistence.room.TypeConverter;

import eu.davidea.avocado.viewmodels.enums.EnumAuthority;
import eu.davidea.avocado.viewmodels.enums.EnumMenuStatus;
import eu.davidea.avocado.viewmodels.enums.EnumRestaurantStatus;
import eu.davidea.avocado.viewmodels.enums.EnumUserStatus;

public class EnumConverters {

    @TypeConverter
    public static String fromEnum(EnumAuthority value) {
        return value.name();
    }

    @TypeConverter
    public static String fromEnum(EnumMenuStatus value) {
        //TODO: Check null values
        return value.name();
    }

    @TypeConverter
    public static String fromEnum(EnumRestaurantStatus value) {
        //TODO: Check null values
        return value.name();
    }

    @TypeConverter
    public static String fromEnum(EnumUserStatus value) {
        return value.name();
    }

    @TypeConverter
    public static EnumAuthority authorityToString(String value) {
        return EnumAuthority.valueOf(value);
    }

    @TypeConverter
    public static EnumMenuStatus menuStatusToString(String value) {
        return EnumMenuStatus.valueOf(value);
    }

    @TypeConverter
    public static EnumRestaurantStatus restaurantStatusToString(String value) {
        return EnumRestaurantStatus.valueOf(value);
    }

    @TypeConverter
    public static EnumUserStatus userStatusToString(String value) {
        return EnumUserStatus.valueOf(value);
    }

}