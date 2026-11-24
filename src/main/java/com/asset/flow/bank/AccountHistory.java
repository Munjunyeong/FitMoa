package com.asset.flow.bank;

import java.time.LocalDateTime;

public class AccountHistory {

    private Long historyId;
    private String bankAccountId;
    private String txType;
    private long amount;
    private String counterpartyAccountId;
    private long balanceAfter;
    private String txMemo;
    private LocalDateTime createdAt;

    public Long getHistoryId() { return historyId; }
    public void setHistoryId(Long historyId) { this.historyId = historyId; }
    public String getBankAccountId() { return bankAccountId; }
    public void setBankAccountId(String bankAccountId) { this.bankAccountId = bankAccountId; }
    public String getTxType() { return txType; }
    public void setTxType(String txType) { this.txType = txType; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    public String getCounterpartyAccountId() { return counterpartyAccountId; }
    public void setCounterpartyAccountId(String counterpartyAccountId) { this.counterpartyAccountId = counterpartyAccountId; }
    public long getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(long balanceAfter) { this.balanceAfter = balanceAfter; }
    public String getTxMemo() { return txMemo; }
    public void setTxMemo(String txMemo) { this.txMemo = txMemo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
