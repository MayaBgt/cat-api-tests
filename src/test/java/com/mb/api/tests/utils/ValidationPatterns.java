package com.mb.api.tests.utils;

import java.util.regex.Pattern;

public class ValidationPatterns {
    public static final Pattern WEIGHT_RANGE = Pattern.compile("\\d+\\s*-\\s*\\d+");
    public static final Pattern CATEGORY_NAME = Pattern.compile("^[a-z]+$");
    public static final Pattern IMAGE_URL = Pattern.compile("^https://cdn2\\.thecatapi\\.com/images/[\\w-]+\\.(jpg|png|gif)$");
}
