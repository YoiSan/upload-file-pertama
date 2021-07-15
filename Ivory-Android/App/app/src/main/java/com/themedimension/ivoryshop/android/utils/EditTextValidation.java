package com.themedimension.ivoryshop.android.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Andreea Braesteanu on 8/23/2017.
 */

public class EditTextValidation {

    public static String isFirstNameValid(String name) {
        if (name.isEmpty())
            return "Please enter your first name.";
        if ((Pattern.matches("^[\\p{L} '-]+$", name))) {
            return "Success";
        }
        return "First name cannot contain special characters";
    }

    public static String isLastNameValid(String name) {
        if (name.isEmpty())
            return "Please enter your last name.";
        if ((Pattern.matches("^[\\p{L} '-]+$", name))) {
            return "Success";
        }
        return "Last name cannot contain special characters.";
    }

    public static String isCardHolderNameValid(String name) {
        if (name.isEmpty())
            return "Please enter your card holder name.";
        if ((Pattern.matches("^[\\p{L} '-]+$", name))) {
            return "Success";
        }
        return "Card holder name cannot contain special characters.";
    }

    //Method to check if is a valid email address (ex: aaa@aa.aa is VALID ,  aa@aa@aaa.aa is NOT VALID)
    public static String isValidEmail(String target) {
        if (target.isEmpty())
            return "Please enter your email.";
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()) {
            for (int i = 0; i < target.length(); i++) {
                if (target.charAt(i) == ' ') {
                    return "Email cannot contain spaces.";
                }
            }
            return "Success";
        } else {
            return "Please enter a valid email address.";
        }
    }

    /*Function to check if is a valid phone number */
    public static String isPhoneNumberValid(String phoneNumber) {
        if (phoneNumber.isEmpty())
            return "Please enter your phone number.";

        for (char c : phoneNumber.toCharArray()) {
            if (Character.isDigit(c)) {
                continue;
            }

            return "Phone number cannot contain special characters.";
        }

        return (phoneNumber.length() >= 10 && phoneNumber.length() <= 20) ? "Success" : "Phone number must have 10 digits length.";
    }

    public static String isStreetValid(String street) {
        if (street.isEmpty())
            return "Street cannot be empty.";
        if ((Pattern.matches("^[\\p{L} .'-]+$", street))) {
            return "Success";
        }
        return "Street cannot contain special characters.";
    }

    public static String isCityValid(String city) {
        if (city.isEmpty())
            return "City cannot be empty.";
        if ((Pattern.matches("^[\\p{L} .'-]+$", city))) {
            return "Success";
        }
        return "City contain special characters.";
    }

    public static String isCountryValid(String country) {
        if (country.isEmpty())
            return "Country cannot be empty.";
        if ((Pattern.matches("^[\\p{L} .'-]+$", country))) {
            return "Success";
        }
        return "Country cannot contain special characters.";
    }

    public static String isZipValid(String zip) {
        if (zip.isEmpty())
            return "Zip code cannot be empty.";
        for (char c : zip.toCharArray()) {
            if (Character.isDigit(c) || Character.isLetter(c) || Character.isSpaceChar(c)) {
                continue;
            }

            return "Zip code cannot contain special characters.";
        }
        return "Success";
    }

    public static String isDistrictValid(String district) {
        if (district.isEmpty())
            return "District cannot be empty.";
        for (char c : district.toCharArray()) {
            if (Character.isDigit(c) || Character.isLetter(c) || Character.isSpaceChar(c)) {
                continue;
            }

            return "District cannot contain special characters.";
        }
        return "Success";
    }

    /*
    function to check if is a valid number (street number)
     */
    public static String isStreetNumberValid(String streetNumber) {
        if (streetNumber.isEmpty())
            return "Street number cannot be empty.";

        for (char c : streetNumber.toCharArray()) {
            if (Character.isDigit(c)) {
                continue;
            }

            return "Street number cannot contain special characters.";
        }

        return "Success";
    }



    public static String isCardNumberValid(String cardNumber) {
        if (cardNumber.isEmpty())
            return "Card number cannot be empty.";
        if (cardNumber.length() < 13)
            return "Card number must have between 13 and 16 digits.";
        for (char c : cardNumber.toCharArray()) {
            if (Character.isDigit(c) || c == '-') {
                continue;
            }

            return "Card number cannot contain special characters.";
        }

        return "Success";
    }

    public static String isCVSValid(String CVS) {
        if (CVS.isEmpty())
            return "CVS cannot be empty.";
        if (CVS.length() < 3)
            return "CVS must have 3 digits.";
        for (char c : CVS.toCharArray()) {
            if (Character.isDigit(c)) {
                continue;
            }

            return "CVS cannot contain special characters.";
        }

        return "Success";
    }


    /**
     * This method should be used to check if the input is a valid password
     */
    public static String isPasswordValid(String input) {


//        return !(input == null || input.isEmpty()) && !input.toLowerCase().equals(input) && input.length() >= 8;
        if (input.isEmpty())
            return "Password cannot be empty.";;

        String expression = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{8,}$";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(input);
        return matcher.find() && input.length() >= 8 ? "Success" : "Invalid Password.";
    }

    /**
     * This method should be used to check if the input is a valid password
     */
    public static String isConfirmPasswordValid(String input) {
        if (input.isEmpty())
            return "Please confirm your password.";

        String expression = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{8,}$";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(input);
        return matcher.find() && input.length() >= 8 ? "Success" : "Invalid Password.";
    }


}
