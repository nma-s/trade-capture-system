package com.technicalchallenge.rsql;

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.OrNode;
import cz.jirutka.rsql.parser.ast.RSQLVisitor;
import jakarta.persistence.criteria.Root;

import java.util.List;
import java.util.function.Predicate;

//public class TradeRSQLVisitor implements RSQLVisitor<Predicate, Root<T>> {
//
//    @Override
//    public List<Predicate> visit(AndNode node, Root<T> param) {
//        return node.getChildren().stream().map(child -> child.accept(this, root)).toList();
//        // Logic for handling an AND operation
//        // You'll need to recursively call accept on child nodes
//        // e.g., node.getChildren().forEach(child -> child.accept(this, param));
//    }
//
//    @Override
//    public R visit(OrNode node, A param) {
//        // Logic for handling an OR operation
//        return null;
//    }
//
//    @Override
//    public R visit(ComparisonNode node, A param) {
//        // Logic for handling a comparison (e.g., ==, !=, >=)
//        String fieldName = node.getSelector();
//        List<String> values = node.getArguments();
//        String operator = node.getOperator().getSymbol();
//        // Use fieldName, values, and operator to build your specific query logic
//        return null;
//    }
//}
