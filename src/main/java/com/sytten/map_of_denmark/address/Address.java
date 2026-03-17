package com.sytten.map_of_denmark.address;
import java.util.regex.Pattern;

public class Address {
    public final String street, house, floor, side, postcode, city;

    private Address(
            String _street, String _house, String _floor, String _side,
            String _postcode, String _city) {
        street = _street;
        house = _house;
        floor = _floor;
        side = _side;
        postcode = _postcode;
        city = _city;
    }

    public String toString() {
        return street + " " + house + ", " + floor + " " + side + "\n"
                + postcode + " " + city;
    }

    private final static String REGEX = "(?<street>[A-Za-zÆÅØåæø ]+) (?<house>[0-9]+)(?: (?<floor>[0-9]+))?(?: (?<side>[A-Za-z]+))? (?<postcode>[0-9]{4}) (?<city>[A-Za-zÆÅØæåø ]+)";
    /* Our regex allows Street + house + postcode + city with floor and side being optionairy. */
    private final static Pattern PATTERN = Pattern.compile(REGEX);

    public static Address parse(String input) {
        Builder builder = new Builder();
        java.util.regex.Matcher matcher = PATTERN.matcher(input);

        if (matcher.matches()){
            builder.street = matcher.group("street");
            builder.house = matcher.group("house");
            builder.floor = matcher.group("floor") != null ? matcher.group("floor") : ""; //If floor is not equal null else if it is true that it is null it will be set to "" instead of null.
            builder.side = matcher.group("side") != null ? matcher.group("side") : ""; //If side is not equal null else if it is true that it is null it will be set to "" instead of null.
            builder.postcode = matcher.group("postcode");
            builder.city = matcher.group("city");
        }

        return builder.build();
    }
    public static class Builder {
        private String street, house, floor, side, postcode, city;

        public Builder street(String _street) {
            street = _street;
            return this;
        }

        public Builder house(String _house) {
            house = _house;
            return this;
        }

        public Builder floor(String _floor) {
            floor = _floor;
            return this;
        }

        public Builder side(String _side) {
            side = _side;
            return this;
        }

        public Builder postcode(String _postcode) {
            postcode = _postcode;
            return this;
        }

        public Builder city(String _city) {
            city = _city;
            return this;
        }

        public Address build() {
            return new Address(street, house, floor, side, postcode, city);
        }
    }

}
