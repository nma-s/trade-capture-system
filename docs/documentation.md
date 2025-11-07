# Trading Application Technical Challenge

This document explains the changes made to the unit tests, why they were required, and the impact of each fix. It also explains the sets I took to implement the enhancement features.

## Step 2: Fix Failing Test Cases 

---

<h3 style="text-align: center;">  Trade Controller Tests </h3>

---

Within the TradeControllerTest the following tests had failed: 
   
1. testDeleteTrade()
2. testCreateTradeValidationFailure_MissingBook()
3. testUpdateTradeIdMismatch()
4. testCreateTrade()
5. testUpdateTrade()
6. estCreateTrade_Validation_Missing_TradeDates()


### 1. testDeleteTrade 

**Problem:**
testDeleteTrade() was failing because the endpoint returned 200 OK, but the test expected 204 No Content.

**Root Cause:**

TradeController.deleteTrade() used:

```
ResponseEntity.ok().body(...)
```


which returns a 200 response and includes a response body.
A proper delete operation should return 204 and no body.

**Solution:**

I updated the controller return statement to:

```
return ResponseEntity.noContent().build();
```

which correctly sends a 204 No Content response.

**Impact:**

✅ The API now follows correct REST standards

✅ The test passes as expected

✅ Response has no unnecessary body payload

### 2. testCreateTradeValidationFailure_MissingBook

**Problem:**

The test testCreateTradeValidationFailure_MissingBook() was failing as it returned 201 Created instead of returning 400 Bad Request. It should not have created a trade as the tradDTO did not have a bookName.

**Root Cause:**

TradeDTO did not enforce validation rules on bookName, so a null or empty input for this field was allowed.

Since the validation was not triggered, the controller proceeded with trade creation, causing the test to fail.

Additionally, once validation was added, the test failed again because there was no standard error response body as the validation exceptions returned empty responses.

**Solution:**

 - I added a ```@NotNull``` constraint to the bookName field in the TradeDTO class.

 - I implemented a ```@ControllerAdvice``` with a global exception handler to map MethodArgumentNotValidException to a 400 response and convert validation error into the required response body - "Book and Counterparty are required"

**Impact:**

✅ Request with missing bookName now fails validation before reaching the service, preventing creation of invalid trades.

✅ Controller now returns 400 Bad Request with a meaningful response body

---
### 3. testUpdateTradeIdMismatch()

**Problem:**

The test testUpdateTradeIdMismatch() was failing because the controller returned 200 OK even when the tradeId in the request body did not match the path variable.
Additionally, the response body was empty instead of providing an error message.

**Root Cause:**

TradeController.updateTrade() did not validate whether:

``/trades/{id}``

matched:

`tradeDTO.getTradeId()`


As a result, inconsistent requests were still processed and returned success.

**Solution:**

To fix this I added a tradeId validation inside updateTrade() to reject inconsistent ids before calling service layer:

``` 
if (!tradeDTO.getTradeId().equals(id)) {    
return ResponseEntity
            .badRequest()
            .body("Trade ID in path must match Trade ID in request body");
 } 
```

**Impact:**

✅ Prevents updates to the wrong trade

✅ Returns 400 Bad Request with a meaningful message


### 4. testCreateTrade()

**Problem:**

testCreateTrade() was failing because the controller returned 201 Created, but the test expected 200 OK after successfully creating a trade.

**Root Cause:**

The createTrade() method used:

```
return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(savedTrade);

```

Since .status(HttpStatus.CREATED) forces a 201 status, the test failed.

**Solution:**

I updated the return statement to:

```
return ResponseEntity.ok(savedTrade);
```

This returns 200 OK, aligning behavior of the createTrade method with test expectations.

**Impact:**

✅ testCreateTrade() now passes with 200 OK status


### 5. testUpdateTrade()

**Problem:**

testUpdateTrade() was failing because the response body was empty, which caused the an assertion error` No value at JSON path $.tradeId.`

**Root Cause:**

The test incorrectly stubbed:

````
when(tradeService.saveTrade()).thenReturn(...)
````

However, updateTrade() does not call saveTrade().
It calls:

```
tradeService.amendTrade(...)
tradeMapper.toDto(...)
```

Since these were not stubbed, the controller returned null, producing an empty response.

**Solution:**

I stubbed the correct methods and removed unnecessary doNothing() stubbing:
````
 when(tradeService.amendTrade(eq(tradeId), any(TradeDTO.class))).thenReturn(trade);
 when(tradeMapper.toDto(trade)).thenReturn(tradeDTO);
````


**Impact:**

✅ Response body is no longer null

✅ JSON path contains a tradeId as expected

### 6. testCreateTrade_Validation_Missing_TradeDates()

**Problem:**

testCreateTrade_Validation_Missing_TradeDates() was failing because the controller returned a generic or incorrect validation message when tradeDate was missing and the test expected a 400 Bad Request with a clear message "Trade date is required".

**Root Cause:**

The method argument validation produced a `MethodArgumentNotValidException`, but the global exception handler returned the same generic message for all validation failures (e.g., "Book and Counterparty are required"). As a result the test could not distinguish the missing tradeDate case from other validation errors.


**Solution:**


 - I enhanced the `MethodArgumentNotValidException` handler to inspect BindingResult and FieldErrors and return field specific messages.

 - I updated the exception handler to return `ResponseEntity.badRequest().body("Trade date is required") ` when the tradeDate field is the failing field

**Impact:**

✅ Requests with a missing trade date now return 400 Bad Request with a clear, field-specific error message.

✅ test passes 

---

<h3 style="text-align: center;">  Trade Leg Controller Tests </h3>

---

Within the TradeLegControllerTest the following test failed.

###  1. testCreateTradeLegValidationFailure_NegativeNotional()

**Problem:**  
  `testCreateTradeLegValidationFailure_NegativeNotional()` asserted a 400 Bad Request but received an incorrect error message instead of the expected message.

**Root Cause:**  
  The global `@ControllerAdvice` handler for `MethodArgumentNotValidException` was returning messages only for missing book, counterparty, or trade dates. Any other invalid fields, such as negative `notional`, produced either a generic message or an empty body, causing the test to fail.

 **Solution:**
   - I enhanced the exception handler to inspect `BindingResult` and `FieldError`s.
   - I added conditional logic to check if the field error was related to the `notional` and returned a specific message: `"Notional must be positive"`.


- **Impact:**
   ✅ Requests with negative `notional` now fail with 400 Bad Request and a clear, specific error message.
   ✅ test passes successfully.


---

<h3 style="text-align: center;">  Trade Service Tests </h3>

---

Within the TradeServiceTest the following tests had failed:

1. testCreateTrade_InvalidDates_ShouldFail()
2. testCreateTrade_Success()
3. testAmendTrade_Success()
4. testFindBookByNonExistentId()
5. testSaveBook()
6. testFindBookById()


### 1. testCreateTrade_InvalidDates_ShouldFail()

**Problem:**  
  `testCreateTrade_InvalidDates_ShouldFail()` was failing even though the service correctly detected invalid dates.

**Root Cause:**  
  The test asserted an incorrect error message string. The actual exception message from `validateTradeCreation()` was `"Start date cannot be before trade date"`, but the test expected a different value.

**Solution:**  
  - I updated the test to assert the correct exception message: `"Start date cannot be before trade date"`

**Impact:**
   ✅ Test now reflects the real validation logic


### 2. testCreateTrade_Success()

**Problem:**  

`testCreateTrade_Success()` was failing with validation errors:
   - "Book not found or not set"
   - "Counterparty not found or not set"
   - "Trade status not found or not set"

**Root Cause:**  
  The test did not stub the repository lookups for Book, Counterparty or TradeStatus. Since `populateReferenceDataByName()` relies on a tradeDTO having a Book, Counterparty, or TradeStatus the validation failed.

**Solution:**
   - Added `@Mock` for the repositories and created Book and Counterparty objects and set names of the TradeDTO
   - Stubbed:
      - `bookRepository.findByBookName(...)`
     
      - `counterpartyRepository.findByName(...)`
     
      - `tradeStatusRepository.findByTradeStatus(...)`
     
      - `tradeLegRepository.save(...)`

- **Impact:**

  ✅ Trade creation behaves as expected workflow with all required fields
   


### 3. testAmendTrade_Success()

**Problem:**  
  `testAmendTrade_Success()` failed with a NullPointerException because `leg` was null inside `generateCashflows()`.

**Root Cause:**  
  The service calls `createTradeLegsWithCashflows()`, which invokes`tradeLegRepository.save(leg)`. In the test, it was not stubbed, so Mockito returned null, leading to the NPE.

**Solution:**  

- I stubbed tradeLegRepository.save(any(TradeLeg.class)) to return a new TradeLeg() so the service receives a non-null leg.

````
  when(tradeLegRepository.save(any(TradeLeg.class))).thenReturn(new TradeLeg());
````

**Impact**

✅ Prevents the NPE and allows the trade leg and cashflow generation to proceed and the test passes.

---

<h3 style="text-align: center;">  Book Service Tests </h3>

---

Within the BookerviceTest the following tests had failed:

1. testFindBookByNonExistentId()
2. testSaveBook()
3. testFindBookById()

### 4. testFindBookByNonExistentId()


**Problem:**  
  `testFindBookByNonExistentId` failed with a `NullPointerException` because `this.bookMapper` was null.

**Root Cause:**  
  The BookService depends on `BookMapper`, but the test did not mock it, so `getBookById()` attempted to use a null mapper.

**Solution:**  
  - I have added a `@Mock` of `BookMapper` so it can be injected into BookService

**Impact**

   ✅ BookMapper is correctly mocked and eliminates the NPE allowing the test to assert the intended not-found behavior correctly.


### 5. testSaveBook()

**Problem:**
testSaveBook() failed with a NullPointerException when BookMapper.toEntity() and BookMapper.toDto() were invoked.

**Root Cause:**
The `BookMapper` dependency was not mocked in the unit test and no stubs were defined, so the service attempted to use a null mapper.

Solution:

- I added a `@Mock` `BookMapper` to the test  and stubbed `toEntity()` and `toDto()` to return valid mapped objects

**Impact:**

✅ The mapping calls no longer return null, avoiding the NPE inside saveBook()


### 6. 

**Problem:**

`testFindBookById()` was failing with AssertionFailedError because the test expected true but the actual result was false

**Root Cause:**

The `BookMapper` dependency was null in the test and no stubs were defined for `toDto()`, so the service returned null instead of a mapped BookDTO.

**Solution:**

- I stubbed `bookMapper.toDto(book)` to return a BookDTO instance with the same id as the book.

**Impact:**

✅ getBookById() now returns a BookDTO with the correct id

---

## Step 3: Implement Missing Functionality

### Enhancement 1: 


- For the multi-criteria search and pagination, I created a TradeSpecification class to handle all the dynamic search logic.

 - Within TradeSpecification, I implemented separate specifications for each search criterion: counterparty, book, trader, trade status, and trade start dates.

 - For trade dates, I implemented range queries (hasStartDateBetween) as well as isolated before (hasStartDateBefore) and after (hasStartDateFrom) filters to support flexible date searches.

 - I also handled nested entity fields for trade.counterparty.name, trade.book.bookName, trade.traderUser.firstName using JPA joins inside the specifications.

