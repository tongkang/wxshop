package com.tongkang.wxshop.service;

import com.tongkang.wxshop.generator.User;

public class UserContext {

    private static ThreadLocal<User> currentUser = new ThreadLocal<>();

    public static User getCurrentUser() {
        return currentUser.get();
    }

    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }
}
