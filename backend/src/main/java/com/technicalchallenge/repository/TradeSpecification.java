package com.technicalchallenge.repository;

import com.technicalchallenge.model.Book;
import com.technicalchallenge.model.Counterparty;
import com.technicalchallenge.model.Trade;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TradeSpecification {
//    Search by counterparty, book, trader, status, date ranges

    //    counterparty filter
    public static Specification<Trade> hasCounterpartyName(String name) {
        return (root, query, criteriaBuilder) -> {
//            return criteriaBuilder.equal(criteriaBuilder.lower(root
//                    .get("counterparty")
//                    .get("name")), name.toLowerCase());
            return criteriaBuilder.equal(criteriaBuilder.lower(root
                    .join("counterparty").get("name")), name.toLowerCase());
        };
    }

    //    book filter
    public static Specification<Trade> hasBookName(String name) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(criteriaBuilder.lower(root
                    .join("book")
                    .get("name")), name.toLowerCase());
        };
    }

    public static Specification<Trade> hasTraderName(String traderUserName){
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root
                    .get("traderUser")
                    .get("name"), traderUserName);
        };
    }

//    public  static Specification<Trade>  hasStartDateFrom(LocalDate startDate){
//        return (root, query, criteriaBuilder) -> {
//            return criteriaBuilder.greaterThanOrEqualTo(root
//                    .get("tradeStartDate"), startDate);
//        };
//    }


}
