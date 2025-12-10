package com.example.settle.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 领域事件基类
 *
 * 所有领域事件的父类，提供通用属性
 */
public abstract class DomainEvent {

    /**
     * 事件ID（唯一标识）
     */
    private final String eventId;

    /**
     * 事件发生时间
     */
    private final LocalDateTime occurredOn;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
    }

    public String getEventId() {
        return eventId;
    }

    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }

    /**
     * 获取事件类型名称
     *
     * @return 事件类型
     */
    public String getEventType() {
        return this.getClass().getSimpleName();
    }
}
