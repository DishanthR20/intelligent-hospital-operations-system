package com.medisphere.appointment.dto;

import java.time.LocalDateTime;

public record SlotSuggestion(LocalDateTime slot, int estimatedWaitMinutes, String explanation) {
}
