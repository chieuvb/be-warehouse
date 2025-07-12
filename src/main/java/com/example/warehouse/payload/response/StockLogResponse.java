package com.example.warehouse.payload.response;

import com.example.warehouse.enums.StockLogEnum;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StockLogResponse {
    private Long id;
    private StockLogEnum transactionType;
    private String productSku;
    private String warehouseName;
    private String zoneName;
    private int quantityBefore;
    private int quantityChange;
    private int quantityAfter;
    private String actorUsername;
    private String note;
    private LocalDateTime createdAt;
}
// This class represents a response payload for stock log entries, which includes details about inventory transactions.