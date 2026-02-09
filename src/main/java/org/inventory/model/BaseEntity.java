package org.inventory.model;

import java.time.LocalDateTime;

// Base class for models if needed
public abstract class BaseEntity {
    protected Long id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
}
