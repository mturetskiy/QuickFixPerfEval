package ru.home.mtur.quickfix.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.field.*;
import quickfix.fix44.NewOrderSingle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class FixUtils {
    final Logger log = LoggerFactory.getLogger(FixUtils.class);

    public static AtomicLong nextClOrderID = new AtomicLong(System.currentTimeMillis());
    public static Random rnd = new Random();
    public static DateTimeFormatter tradeDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static Message generateOrder(Long msgID) {
        LocalDateTime now = LocalDateTime.now();
        NewOrderSingle order = new NewOrderSingle(
                new ClOrdID("ClOrd-" + nextClOrderID.getAndIncrement()),
                new Side(rnd.nextBoolean() ? Side.SELL : Side.BUY),
                new TransactTime(now),
                new OrdType(OrdType.MARKET));
        order.set(new TradeDate(tradeDateFormatter.format(now)));
        order.set(new SettlType(SettlType.T_3));
        order.set(new SecurityID("IBM.L"));
        order.set(new SecurityIDSource(SecurityIDSource.RIC_CODE));
        order.set(new SettlCurrency("GBp"));
        order.set(new Currency("GBp"));
        order.set(new Text("Order-" + msgID));
        order.set(new Symbol("IBM.L"));

        return order;
    }
}
