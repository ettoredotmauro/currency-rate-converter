package pl.cleankod.exchange.core.dto;

public record AccountDto(String id, String number, MoneyDto balance) {}