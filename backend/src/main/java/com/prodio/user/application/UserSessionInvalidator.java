package com.prodio.user.application;

public interface UserSessionInvalidator {
    /** 해당 유저의 활성 세션을 모두 즉시 만료시킨다. */
    void invalidateSessionsOf(long userId);
}
