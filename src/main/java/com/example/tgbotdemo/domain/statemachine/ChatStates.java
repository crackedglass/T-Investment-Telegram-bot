package com.example.tgbotdemo.domain.statemachine;

public enum ChatStates {

    // Main menu
    MAIN, ORDER_ASKING_CELL, ORDER_ASKING_AMOUNT,
    ASKING_ACTION, ASKING_TO_DELETE, INFO_ASKING_CELL,

    // Admin menu
    ADMIN, LOADING_USERS, LOADING_MAP, ADDING_MONEY, NEW_ROUND, APPLY_CHANGES
}
