package com.BankingAPI.BankingAPI.Group1.service;

import com.BankingAPI.BankingAPI.Group1.config.BeanFactory;
import com.BankingAPI.BankingAPI.Group1.model.Account;
import com.BankingAPI.BankingAPI.Group1.model.Enums.AccountType;
import com.BankingAPI.BankingAPI.Group1.model.Transaction;
import com.BankingAPI.BankingAPI.Group1.model.Users;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransactionGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransferMoneyPOSTResponse;
import com.BankingAPI.BankingAPI.Group1.repository.AccountRepository;
import com.BankingAPI.BankingAPI.Group1.repository.TransactionRepository;
import com.BankingAPI.BankingAPI.Group1.repository.specification.TransactionSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final BeanFactory beanFactory;
    private final AccountRepository accountRepository;
    @Autowired
    private AccountService accountService;
    @Autowired
    private UserService userService;

    public TransactionService(TransactionRepository transactionRepository, BeanFactory beanFactory, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.beanFactory = beanFactory;
        this.accountRepository = accountRepository;
    }

    public TransactionGETPOSTResponseDTO transferToOtherCustomer(TransferMoneyPOSTResponse transactionDTO) throws Exception {
        beanFactory.validateAuthentication();
        Users user = beanFactory.getCurrentUser();
        Account fromAccount = getAccount(transactionDTO.fromAccount());
        Account toAccount = getAccount(transactionDTO.toAccount());

        if (fromAccount.getAccountType() == AccountType.SAVINGS && toAccount.getAccountType() == AccountType.SAVINGS) {
            throw new Exception("Cannot transfer money between savings accounts.");
        }

        if (fromAccount.getUser().getId() == toAccount.getUser().getId()) {
            throw new Exception("Both accounts cannot belong to the same user for this operation.");
        }

        validateTransactionLimits(fromAccount, transactionDTO.amount());
        performTransfer(fromAccount, toAccount, transactionDTO.amount());

        Transaction newTransaction = createAndSaveTransaction(user, fromAccount.getIBAN(), toAccount.getIBAN(), transactionDTO.amount());
        return mapToTransactionResponse(newTransaction);
    }


    public TransactionGETPOSTResponseDTO transferMoneyToOwnAccount(TransferMoneyPOSTResponse transactionDTO) throws Exception {
         beanFactory.validateAuthentication();
         Users user = beanFactory.getCurrentUser();

        Account fromAccount = getAccount(transactionDTO.fromAccount());
        Account toAccount = getAccount(transactionDTO.toAccount());

        validateAccountOwnership(fromAccount, toAccount);
        validateTransactionLimits(fromAccount, transactionDTO.amount());

        performTransfer(fromAccount, toAccount, transactionDTO.amount());
        Transaction newTransaction = createAndSaveTransaction(user, fromAccount.getIBAN(), toAccount.getIBAN(), transactionDTO.amount());

        return mapToTransactionResponse(newTransaction);
    }
    private Account getAccount(String iban) throws Exception {
        Account account = accountRepository.findByIBAN(iban)
                .orElseThrow(() -> new Exception("Account with IBAN: " + iban + " not found"));

        if (account.getUser() == null) {
            throw new Exception("No user associated with the account: " + iban);
        }
        return account;
    }

    private void validateAccountOwnership(Account fromAccount, Account toAccount) throws Exception {
        Long currentUserId = beanFactory.getCurrentUserId();
        if (fromAccount.getUser().getId() != currentUserId || toAccount.getUser().getId() != currentUserId) {
            throw new Exception("Both accounts must belong to the same user");
        }
    }

    private void validateTransactionLimits(Account fromAccount, double amount) throws Exception {
        if (fromAccount.getBalance() < amount) {
            throw new Exception("Insufficient funds");
        }
        if (amount > fromAccount.getUser().getDailyLimit()) {
            throw new Exception("Transaction exceeds daily limit");
        }
    }

    private void performTransfer(Account fromAccount, Account toAccount, double amount) {
        fromAccount.setBalance(fromAccount.getBalance() - amount);
        double dailyLimit = fromAccount.getUser().getDailyLimit();
        fromAccount.getUser().setDailyLimit(dailyLimit - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }

    private Transaction createAndSaveTransaction(Users user, String fromIBAN, String toIBAN, double amount) {
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setFromAccount(fromIBAN);
        transaction.setToAccount(toIBAN);
        transaction.setAmount(amount);
        transaction.setDate(LocalDate.now());
        return transactionRepository.save(transaction);
    }


    private TransactionGETPOSTResponseDTO mapToTransactionResponse(Transaction transaction) {
        return new TransactionGETPOSTResponseDTO(
                transaction.getFromAccount(),
                transaction.getToAccount(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getUser().getId()
        );
    }

    public List<TransactionGETPOSTResponseDTO> getTransactionsByUserId(Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
           return transactions.stream()
                .map(transaction -> new TransactionGETPOSTResponseDTO(
                        transaction.getFromAccount(),
                        transaction.getToAccount(),
                        transaction.getAmount(),
                        transaction.getDate(),
                        transaction.getUser().getId()
                ))
                .collect(Collectors.toList());
    }



    public TransactionGETPOSTResponseDTO processWithdrawal(TransactionGETPOSTResponseDTO transactionDTO) throws IllegalArgumentException, IllegalStateException {
        Users user = beanFactory.getCurrentUser();
        Account account = validateAccount(transactionDTO.userId());

        checkAndUpdateDailyLimit(user, transactionDTO.amount());
        updateAccountBalance(account, -transactionDTO.amount());

        Transaction transaction = new Transaction(user, account.getIBAN(), "ATM", transactionDTO.amount(), transactionDTO.date());
        transactionRepository.save(transaction);

        return new TransactionGETPOSTResponseDTO(
                account.getIBAN(),
                "ATM",
                transactionDTO.amount(),
                transactionDTO.date(),
                user.getId()
        );
    }

    private void updateAccountBalance(Account account, double amountChange) throws IllegalStateException {
        double newBalance = account.getBalance() + amountChange;
        if (newBalance < account.getAbsoluteLimit()) {
            throw new IllegalStateException("Withdrawal exceeds absolute limit");
        }
        account.setBalance(newBalance);
        accountService.save(account);
    }
    private Account validateAccount(Long userId) throws IllegalArgumentException {
        Account account = accountService.findById(userId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }
        return account;
    }
    private void checkAndUpdateDailyLimit(Users user, double amount) throws IllegalStateException {
        if (!userService.checkAndUpdateDailyLimit(user, amount)) {
            throw new IllegalStateException("Daily limit exceeded");
        }
    }

    public TransactionGETPOSTResponseDTO processDeposit(TransactionGETPOSTResponseDTO transactionDTO) throws IllegalArgumentException {
        Users user = beanFactory.getCurrentUser();
        Account account = validateAccount(transactionDTO.userId());

        updateAccountBalance(account, transactionDTO.amount());

        Transaction transaction = new Transaction(user, "ATM", account.getIBAN(), transactionDTO.amount(), transactionDTO.date());
        transactionRepository.save(transaction);

        return new TransactionGETPOSTResponseDTO(
                "ATM",
                account.getIBAN(),
                transactionDTO.amount(),
                transactionDTO.date(),
                user.getId()
        );
    }

    public List<Transaction> filterTransactions(String IBAN, Double amount, Double amountGreater, Double amountLess, LocalDate startDate, LocalDate endDate) {
        Specification<Transaction> spec = Specification.where(null);

        if (IBAN != null) {
            spec = spec.and(TransactionSpecification.hasIBAN(IBAN));
        }
        if (amount != null) {
            spec = spec.and(TransactionSpecification.amountEquals(amount));
        }
        if (amountGreater != null) {
            spec = spec.and(TransactionSpecification.amountGreaterThan(amountGreater));
        }
        if (amountLess != null) {
            spec = spec.and(TransactionSpecification.amountLessThan(amountLess));
        }
        if (startDate != null && endDate != null) {
            spec = spec.and(TransactionSpecification.isBetweenDates(startDate, endDate));
        }

        return transactionRepository.findAll(spec);
    }
    public void save(Transaction transaction) {
        transactionRepository.save(transaction);
    }


    public List<TransactionGETPOSTResponseDTO> allTransactions() throws Exception {
      //  beanFactory.validateAuthentication();
        List<Transaction> transactions = transactionRepository.findAll();
            return transactions.stream()
                    .map(transaction -> new TransactionGETPOSTResponseDTO(
                            transaction.getFromAccount(),
                            transaction.getToAccount(),
                            transaction.getAmount(),
                            transaction.getDate(),
                            transaction.getUser().getId()
                    ))
                    .collect(Collectors.toList());
    }

    public List<TransactionGETPOSTResponseDTO> findTransactionsInitializedByCustomers() throws Exception {
        //beanFactory.validateAuthentication();
        List<Transaction> transactions = transactionRepository.findByUserTypeCustomer();
        return transactions.stream()
                .map(transaction -> new TransactionGETPOSTResponseDTO(
                        transaction.getFromAccount(),
                        transaction.getToAccount(),
                        transaction.getAmount(),
                        transaction.getDate(),
                        transaction.getUser().getId()
                ))
                .collect(Collectors.toList());
    }

    public List<TransactionGETPOSTResponseDTO> findTransactionsInitializedByEmployees() throws Exception {
        //beanFactory.validateAuthentication();
        List<Transaction> transactions = transactionRepository.findByUserTypeEmployee();
        return transactions.stream()
                .map(transaction -> new TransactionGETPOSTResponseDTO(
                        transaction.getFromAccount(),
                        transaction.getToAccount(),
                        transaction.getAmount(),
                        transaction.getDate(),
                        transaction.getUser().getId()
                ))
                .collect(Collectors.toList());
    }



    public List<TransactionGETPOSTResponseDTO> findATMTransactions() throws Exception {
        //beanFactory.validateAuthentication();
        List<Transaction> transactions = transactionRepository.findATMTransactions();
        return transactions.stream()
                .map(transaction -> new TransactionGETPOSTResponseDTO(
                        transaction.getFromAccount(),
                        transaction.getToAccount(),
                        transaction.getAmount(),
                        transaction.getDate(),
                        transaction.getUser().getId()
                ))
                .collect(Collectors.toList());
    }



    public List<TransactionGETPOSTResponseDTO> findATMTransactionsByUserId(long idOfUser) throws Exception {
        beanFactory.validateAuthentication();
        List<Transaction> transactions = transactionRepository.findATMTransactionsByUserId(idOfUser);
        return transactions.stream()
                .map(transaction -> new TransactionGETPOSTResponseDTO(
                        transaction.getFromAccount(),
                        transaction.getToAccount(),
                        transaction.getAmount(),
                        transaction.getDate(),
                        transaction.getUser().getId()
                ))
                .collect(Collectors.toList());
    }

    public List<TransactionGETPOSTResponseDTO> findATMWithdrawalsByUserId(long idOfUser) throws Exception {
        beanFactory.validateAuthentication();
        List<Transaction> transactions = transactionRepository.findATMWithdrawalsByUserId(idOfUser);
        return transactions.stream()
                .map(transaction -> new TransactionGETPOSTResponseDTO(
                        transaction.getFromAccount(),
                        transaction.getToAccount(),
                        transaction.getAmount(),
                        transaction.getDate(),
                        transaction.getUser().getId()
                ))
                .collect(Collectors.toList());
    }

    public List<TransactionGETPOSTResponseDTO> findATMDepositsByUserId(long idOfUser) throws Exception {
        beanFactory.validateAuthentication();
        List<Transaction> transactions = transactionRepository.findATMDepositsByUserId(idOfUser);
        return transactions.stream()
                .map(transaction -> new TransactionGETPOSTResponseDTO(
                        transaction.getFromAccount(),
                        transaction.getToAccount(),
                        transaction.getAmount(),
                        transaction.getDate(),
                        transaction.getUser().getId()
                ))
                .collect(Collectors.toList());
    }


    public List<TransactionGETPOSTResponseDTO> findOnlineTransactions() throws Exception {
        //beanFactory.validateAuthentication();
        List<Transaction> transactions = transactionRepository.findOnlineTransactions();
        return transactions.stream()
                .map(transaction -> new TransactionGETPOSTResponseDTO(
                        transaction.getFromAccount(),
                        transaction.getToAccount(),
                        transaction.getAmount(),
                        transaction.getDate(),
                        transaction.getUser().getId()
                ))
                .collect(Collectors.toList());
    }

    public List<TransactionGETPOSTResponseDTO> findOnlineTransactionsByEmployees() throws Exception {
        //beanFactory.validateAuthentication();
        List<Transaction> transactions = transactionRepository.findOnlineTransactionsByEmployees();
        return transactions.stream()
                .map(transaction -> new TransactionGETPOSTResponseDTO(
                        transaction.getFromAccount(),
                        transaction.getToAccount(),
                        transaction.getAmount(),
                        transaction.getDate(),
                        transaction.getUser().getId()
                ))
                .collect(Collectors.toList());
    }

    public List<TransactionGETPOSTResponseDTO> findOnlineTransactionsByCustomers() throws Exception {
        //beanFactory.validateAuthentication();
        List<Transaction> transactions = transactionRepository.findOnlineTransactionsByCustomers();
        return transactions.stream()
                .map(transaction -> new TransactionGETPOSTResponseDTO(
                        transaction.getFromAccount(),
                        transaction.getToAccount(),
                        transaction.getAmount(),
                        transaction.getDate(),
                        transaction.getUser().getId()
                ))
                .collect(Collectors.toList());
    }

    public List<TransactionGETPOSTResponseDTO> findOnlineTransactionsByUserId(long idOfUser) throws Exception {
        //beanFactory.validateAuthentication();
        List<Transaction> transactions = transactionRepository.findOnlineTransactionsByUserId(idOfUser);
        return transactions.stream()
                .map(transaction -> new TransactionGETPOSTResponseDTO(
                        transaction.getFromAccount(),
                        transaction.getToAccount(),
                        transaction.getAmount(),
                        transaction.getDate(),
                        transaction.getUser().getId()
                ))
                .collect(Collectors.toList());
    }



}
