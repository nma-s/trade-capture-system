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
                    .get("book")
                    .get("bookName")), name.toLowerCase());
        };
    }
    //    trader first name filter
    public static Specification<Trade> hasTraderName(String traderUserName){
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(criteriaBuilder.lower(root
                    .join("traderUser")
                    .get("firstName")), traderUserName.toLowerCase());
        };
    }
    //    trade status filter
    public static Specification<Trade> hasTradeStatus(String tradeStatus){
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(criteriaBuilder.lower(root
                    .get("tradeStatus")
                    .get("tradeStatus")), tradeStatus.toLowerCase());
        };
    }
    //    date range filter
    public  static Specification<Trade>  hasStartDateBetween(String startDate,String endDate){
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.between(root
                    .get("tradeStartDate"), LocalDate.parse(startDate),LocalDate.parse(endDate));
        };
    }
    //    from date filter
    public  static Specification<Trade>  hasStartDateFrom(String startDate){
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.greaterThanOrEqualTo(root
                    .get("tradeStartDate"), LocalDate.parse(startDate));
        };
    }
    //    before date filter
    public  static Specification<Trade>  hasStartDateBefore(String startDate){
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.lessThanOrEqualTo(root
                    .get("tradeStartDate"), LocalDate.parse(startDate));
        };
    }


}
