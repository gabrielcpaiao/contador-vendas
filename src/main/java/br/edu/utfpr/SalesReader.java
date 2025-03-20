package br.edu.utfpr;

import com.opencsv.bean.CsvToBeanBuilder;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

public class SalesReader {

    private final List<Sale> sales;

    public SalesReader(String salesFile) {

        final var dataStream = ClassLoader.getSystemResourceAsStream(salesFile);

        if (dataStream == null) {
            throw new IllegalStateException("File not found or is empty");
        }

        final var builder = new CsvToBeanBuilder<Sale>(new InputStreamReader(dataStream, StandardCharsets.UTF_8));

        sales = builder
                .withType(Sale.class)
                .withSeparator(';')
                .build()
                .parse();
    }

    public BigDecimal totalOfCompletedSales() {
        return sales.stream()
                .filter(sale -> sale.getStatus() == Sale.Status.COMPLETED)
                .map(Sale::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalOfCancelledSales() {
        return sales.stream()
                .filter(sale -> sale.getStatus() == Sale.Status.CANCELLED)
                .map(Sale::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Optional<Sale> mostRecentCompletedSale() {
        return sales.stream()
                .filter(sale -> sale.getStatus() == Sale.Status.COMPLETED)
                .max(Comparator.comparing(Sale::getSaleDate));
    }

    public long daysBetweenFirstAndLastCancelledSale() {
        return sales.stream()
                .filter(sale -> sale.getStatus() == Sale.Status.CANCELLED)
                .map(Sale::getSaleDate)
                .sorted()
                .toList()
                .stream()
                .reduce((first, last) -> last)
                .map(lastDate -> sales.stream()
                        .filter(sale -> sale.getStatus() == Sale.Status.CANCELLED)
                        .map(Sale::getSaleDate)
                        .min(Comparator.naturalOrder())
                        .map(firstDate -> java.time.temporal.ChronoUnit.DAYS.between(firstDate, lastDate))
                                                                                                          
                        .orElse(0L))
                .orElse(0L);
    }

    public BigDecimal totalCompletedSalesBySeller(String sellerName) {
        return sales.stream()
                .filter(sale -> sale.getStatus() == Sale.Status.COMPLETED)
                .filter(sale -> sale.getSeller().equalsIgnoreCase(sellerName))
                .map(Sale::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long countAllSalesByManager(String managerName) {
        return sales.stream()
                .filter(sale -> sale.getManager().equalsIgnoreCase(managerName))
                .count();
    }

    public BigDecimal totalSalesByStatusAndMonth(Sale.Status status, Month... months) {
        List<Month> monthList = Arrays.asList(months);

        return sales.stream()
                .filter(sale -> sale.getStatus() == status)
                .filter(sale -> monthList.contains(sale.getSaleDate().getMonth()))
                .map(Sale::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<String, Long> countCompletedSalesByDepartment() {
        return sales.stream()
                .filter(sale -> sale.getStatus() == Sale.Status.COMPLETED)
                .collect(Collectors.groupingBy(Sale::getDepartment, Collectors.counting()));
    }

    public Map<Integer, Map<String, Long>> countCompletedSalesByPaymentMethodAndGroupingByYear() {
        return sales.stream()
                .filter(sale -> sale.getStatus() == Sale.Status.COMPLETED)
                .collect(Collectors.groupingBy(
                        sale -> sale.getSaleDate().getYear(),
                        Collectors.groupingBy(
                                Sale::getPaymentMethod,
                                Collectors.counting()
                        )
                ));
    }

    public Map<String, BigDecimal> top3BestSellers() {
        return sales.stream()
                .filter(sale -> sale.getStatus() == Sale.Status.COMPLETED)
                .collect(Collectors.groupingBy(
                        Sale::getSeller,
                        Collectors.mapping(Sale::getValue, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }
}

