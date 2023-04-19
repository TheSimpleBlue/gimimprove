package com.gimimprove;

import java.util.HashSet;
import java.util.Set;

public class UnlockedItem {


    public final int itemID;
    public Set<String> unlockedByUsername;

    UnlockedItem(int itemID) {
        this.itemID=itemID;
        this.unlockedByUsername = new HashSet<>();
    }
    UnlockedItem(int itemID, Set<String> unlockedByUsername) {
        this.itemID=itemID;
        this.unlockedByUsername = unlockedByUsername;
    }

    public void addUser(String user) {
        unlockedByUsername.add(user);
    }
}
