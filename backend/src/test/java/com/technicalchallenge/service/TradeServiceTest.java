package com.technicalchallenge.service;

import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.dto.TradeLegDTO;
import com.technicalchallenge.model.*;
import com.technicalchallenge.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    private ApplicationUserRepository applicationUserRepository;

    @Mock
    private LegTypeRepository legTypeRepository;

    @Mock
    private PayRecRepository payRecRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CounterpartyRepository counterpartyRepository;

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private TradeLegRepository tradeLegRepository;

    @Mock
    private CashflowRepository cashflowRepository;

    @Mock
    private TradeStatusRepository tradeStatusRepository;

    @Mock
    private AdditionalInfoService additionalInfoService;

    @InjectMocks
    private TradeService tradeService;

    private TradeDTO tradeDTO;
    private Trade trade;

//    Added fields for book and counterParty
    private Book book;
    private Counterparty counterParty;
    private ApplicationUser applicationUser;

    @BeforeEach
    void setUp() {
        // Set up test data
        tradeDTO = new TradeDTO();
        tradeDTO.setTradeId(100001L);
        tradeDTO.setTradeDate(LocalDate.of(2025, 1, 15));
        tradeDTO.setTradeStartDate(LocalDate.of(2025, 1, 17));
        tradeDTO.setTradeMaturityDate(LocalDate.of(2026, 1, 17));

        //  Additional validation rules require the TradeLegDTO PayRecieveFlag and not to be null
        TradeLegDTO leg1 = new TradeLegDTO();
        leg1.setNotional(BigDecimal.valueOf(1000000));
        leg1.setRate(0.05);
        //  Additional setup
        leg1.setLegId(1L);
        leg1.setPayReceiveFlag("Receive");
        leg1.setLegType("Fixed");
        leg1.setCalculationPeriodSchedule("3M");

        TradeLegDTO leg2 = new TradeLegDTO();
        leg2.setNotional(BigDecimal.valueOf(1000000));
        leg2.setRate(0.0);
        //  Additional setup
        leg2.setLegId(2L);
        leg2.setPayReceiveFlag("Pay");
        leg2.setLegType("Fixed");
        leg2.setCalculationPeriodSchedule("3M");

        tradeDTO.setTradeLegs(Arrays.asList(leg1, leg2));

        trade = new Trade();
        trade.setId(1L);
        trade.setTradeId(100001L);
        trade.setVersion(1);

//     Set book and counterparty

        book = new Book();
        book.setId(1L);
        book.setBookName("FX-BOOK-2");
        book.setActive(true);

        counterParty = new Counterparty();
        counterParty.setId(1L);
        counterParty.setName("GiantCash");
        counterParty.setActive(true);

        applicationUser = new ApplicationUser();
        applicationUser.setActive(true);


        tradeDTO.setBookName(book.getBookName());
        tradeDTO.setCounterpartyName(counterParty.getName());
        //
        tradeDTO.setBookId(book.getId());
        tradeDTO.setCounterpartyId(counterParty.getId());
        tradeDTO.setTraderUserId(1L);
        tradeDTO.setTraderUserName("Simon King");
    }

    @Test
    void testCreateTrade_Success() {


//      tradeDTO.setTradeStatus(tradeStatus.getTradeStatus());

        // Given
        when(bookRepository.findByBookName("FX-BOOK-2")).thenReturn(Optional.of(book));
        when(counterpartyRepository.findByName("GiantCash")).thenReturn(Optional.of(counterParty));
        when(tradeStatusRepository.findByTradeStatus("NEW")).thenReturn(Optional.of(new com.technicalchallenge.model.TradeStatus()));
        when(legTypeRepository.findByType(any(String.class))).thenReturn(Optional.of(new LegType()));
        when(payRecRepository.findByPayRec(any(String.class))).thenReturn(Optional.of(new PayRec()));
        when(applicationUserRepository.findByFirstName(any(String.class))).thenReturn(Optional.of(applicationUser));
        when(tradeLegRepository.save(any(TradeLeg.class))).thenReturn(new com.technicalchallenge.model.TradeLeg());
        when(tradeRepository.save(any(Trade.class))).thenReturn(trade);

        // When

        Trade result = tradeService.createTrade(tradeDTO);

        // Then
        assertNotNull(result);
        assertEquals(100001L, result.getTradeId());
        verify(tradeRepository).save(any(Trade.class));
    }

    @Test
    void testCreateTrade_InvalidDates_ShouldFail() {
        // Given - This test is intentionally failing for candidates to fix
        tradeDTO.setTradeStartDate(LocalDate.of(2025, 1, 10)); // Before trade date

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tradeService.createTrade(tradeDTO);
        });

        // This assertion is intentionally wrong - candidates need to fix it
        // NS: Changed from "Wrong error message" to "Start date cannot be before trade date"
        assertEquals("Start date cannot be before trade date", exception.getMessage());
    }

    @Test
    void testCreateTrade_InvalidLegCount_ShouldFail() {
        // Given
        tradeDTO.setTradeLegs(Arrays.asList(new TradeLegDTO())); // Only 1 leg

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tradeService.createTrade(tradeDTO);
        });

        assertTrue(exception.getMessage().contains("exactly 2 legs"));
    }

    @Test
    void testGetTradeById_Found() {
        // Given
        when(tradeRepository.findByTradeIdAndActiveTrue(100001L)).thenReturn(Optional.of(trade));

        // When
        Optional<Trade> result = tradeService.getTradeById(100001L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(100001L, result.get().getTradeId());
    }

    @Test
    void testGetTradeById_NotFound() {
        // Given
        when(tradeRepository.findByTradeIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        // When
        Optional<Trade> result = tradeService.getTradeById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testAmendTrade_Success() {

        tradeDTO.setTradeStatus("AMENDED");
        // Given
        when(tradeRepository.findByTradeIdAndActiveTrue(100001L)).thenReturn(Optional.of(trade));
        when(tradeStatusRepository.findByTradeStatus("AMENDED")).thenReturn(Optional.of(new com.technicalchallenge.model.TradeStatus()));
        when(tradeLegRepository.save(any(TradeLeg.class))).thenReturn(new com.technicalchallenge.model.TradeLeg());
        when(tradeRepository.save(any(Trade.class))).thenReturn(trade);

        // When
        Trade result = tradeService.amendTrade(100001L, tradeDTO);

        // Then
        assertNotNull(result);
        verify(tradeRepository, times(2)).save(any(Trade.class)); // Save old and new
    }

    @Test
    void testAmendTrade_TradeNotFound() {
        // Given
        when(tradeRepository.findByTradeIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tradeService.amendTrade(999L, tradeDTO);
        });

        assertTrue(exception.getMessage().contains("Trade not found"));
    }

    // This test has a deliberate bug for candidates to find and fix
    @Test
    void testCashflowGeneration_MonthlySchedule() {
        // This test method is incomplete and has logical errors
        // Candidates need to implement proper cashflow testing

        // Given - setup is incomplete

        Cashflow cashflow = new Cashflow();

        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setSchedule("6M");

        //  Set up TradeLegs
        TradeLeg leg1 = new TradeLeg();
        leg1.setNotional(BigDecimal.valueOf(1000000));
        leg1.setRate(0.05);
        leg1.setLegId(1L);
        leg1.setCalculationPeriodSchedule(schedule);
        leg1.setCashflows(Arrays.asList(cashflow,cashflow));

        TradeLeg leg2 = new TradeLeg();
        leg2.setNotional(BigDecimal.valueOf(1000000));
        leg2.setRate(0.05);
        leg2.setLegId(2L);
        leg2.setCalculationPeriodSchedule(schedule);
        leg2.setCashflows(Arrays.asList(new Cashflow(),new Cashflow()));

        trade.setTradeLegs(Arrays.asList(leg1,leg2));
        trade.setTradeStartDate(LocalDate.of(2025, 1, 17));
        trade.setTradeMaturityDate(LocalDate.of(2026,1,17));

        //    Set up TradeDTO (set required fields - book, counterparty, trade status)
        tradeDTO.setBookName(book.getBookName());
        tradeDTO.setCounterpartyName(counterParty.getName());
        tradeDTO.setTradeStatus("NEW");


        when(bookRepository.findByBookName(any(String.class))).thenReturn(Optional.of(book));
        when(counterpartyRepository.findByName(any(String.class))).thenReturn(Optional.of(counterParty));
        when(tradeStatusRepository.findByTradeStatus(any(String.class))).thenReturn(Optional.of(new TradeStatus()));//        when(legTypeRepository.findByType("Fixed")).thenReturn(Optional.of(legType));
        when(scheduleRepository.findBySchedule(any(String.class))).thenReturn(Optional.of(schedule));
        when(tradeLegRepository.save(any(TradeLeg.class))).thenReturn(new TradeLeg());
        when(tradeRepository.save(any(Trade.class))).thenReturn(trade);


        // When - method call is missing
        Trade result = tradeService.createTrade(tradeDTO);

        int cashflowCount = 0;

        for(int i = 0; i < result.getTradeLegs().size(); i++){

            cashflowCount += result.getTradeLegs().get(i).getCashflows().size();
        }

        // Then - assertions are wrong/missing
        assertEquals(4, cashflowCount); // This will always fail - candidates need to fix
    }
}
