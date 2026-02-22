package com.yori3o.boss_checklist.common.client.boss;


import com.yori3o.boss_checklist.common.client.data.ClientBossAttempt;


/**
 * This class contains information about the boss's status.
 */
public class BossProgress {


    private boolean defeated;
    private boolean markedAsDefeatedOnClient;
    public boolean alreadyAnimated;
    private boolean fresh;
    private String killerName;

    private ClientBossAttempt lastAttempt;


    public boolean isDefeated() {
        return defeated;
    }

    public boolean isMarkedAsDefeatedOnClient() {
        return markedAsDefeatedOnClient;
    }

    public boolean isFresh() {
        return fresh;
    }

    public String killerName() {
        return killerName;
    }

    public ClientBossAttempt lastAttempt() {
        return lastAttempt;
    }

    public void markDefeated(String killer, boolean fresh, ClientBossAttempt attempt) {
        this.defeated = true;
        this.fresh = fresh;
        this.killerName = killer;
        this.lastAttempt = attempt;
        this.alreadyAnimated = !fresh;
    }

    public void markDefeatedClient(boolean bool) {
        this.markedAsDefeatedOnClient = bool;
    }

    public void markNotDefeated() {
        this.defeated = false;
        this.fresh = false;
        this.killerName = null;
        this.lastAttempt = null;
    }

    public void clearFreshFlag() {
        this.fresh = false;
        this.alreadyAnimated = true;
    }
}
