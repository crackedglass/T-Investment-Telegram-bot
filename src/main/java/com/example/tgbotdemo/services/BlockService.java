package com.example.tgbotdemo.services;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

// Using for blocking new orders
@Slf4j
@Service
public class BlockService {

    private boolean blocked;

    public BlockService() {
        blocked = false;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked() {
        log.info("All trades are blocked");
        this.blocked = true;
    }

    public void removeBlocked() {
        log.info("All trades are unblocked");
        this.blocked = false;
    }
}
