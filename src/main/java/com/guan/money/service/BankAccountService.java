package com.guan.money.service;

import java.math.BigDecimal;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.persistence.*;

import org.javamoney.moneta.Money;
import org.junit.Assert;
import org.springframework.stereotype.Service;

import com.guan.money.entity.BankAccount;

@Service
public class BankAccountService {

    private EntityManagerFactory entityManagerFactory;

    private ThreadLocal<EntityManager> entityManagerThreadLocal = new ThreadLocal<EntityManager>();

    public BankAccountService() {

        this.entityManagerFactory = Persistence.createEntityManagerFactory("BankAccount-unit");

        //init TestAcountA&TestAcountB in H2 database
        createBankAccount("TestAcountA", "HKD", new BigDecimal(100000));
        createBankAccount("TestAcountB", "HKD", new BigDecimal(100000));
    }

    /**
     * get thread local entityManager to avoid thread safe issue
     * @return
     */
    private synchronized EntityManager getEntityManager() {
        EntityManager entityManager = entityManagerThreadLocal.get();
        if (entityManager == null) {
            entityManager = entityManagerFactory.createEntityManager();
            entityManagerThreadLocal.set(entityManager);
        }

        return  entityManager;
    }

    public synchronized void closeEntityManager() {
        EntityManager entityManager = entityManagerThreadLocal.get();
        if (entityManager != null) {
            entityManager.clear();
            entityManager.close();
            entityManagerThreadLocal.set(null);
        }
    }


    /**
     * Create a new bank account
     * @param name
     * @param currency
     * @param balance
     */
    public void createBankAccount(String name, String currency, BigDecimal balance) {
        EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin(); // Start transaction
            BankAccount account = new BankAccount();
            account.setName(name);
            account.setCurrency(currency);
            account.setBalance(balance);
            entityManager.persist(account); // Save to DB
            transaction.commit(); // Commit transaction
        } catch (RuntimeException e) {
            transaction.rollback(); // Rollback on error
            throw e;
        }

    }

    /**
     * retrieve account by account name and lock the account row in db
     * @param accountName
     * @return
     */
    public BankAccount getBankAcount(String accountName) {
        EntityManager entityManager = getEntityManager();
        return entityManager.createQuery("SELECT e FROM BankAccount e where e.name = '" + accountName + "'", BankAccount.class)
                .getResultList().get(0);
    }

    /**
     * update bank account balance by Optimistic Lock
     * @param bankAccount
     */
    public void updateBankAccountBalanceWithOptimisticLock(BankAccount bankAccount) {
        EntityManager entityManager = getEntityManager();

        String sql = "update BankAccount e set e.balance = " + bankAccount.getBalance() + ", e.version = e.version + 1 where e.name = '"
                + bankAccount.getName() + "' and e.version = " + bankAccount.getVersion();
        Query query = entityManager.createNativeQuery(sql, BankAccount.class);

        int updateRows = query.executeUpdate();

        if (updateRows == 0) {
            throw new RuntimeException("Account data has been changed, please retry again.");
        }
    }

    /**
     * transfer money for two accounts
     * @param fromAccountName
     * @param toAccountName
     * @param amount
     * @param currency
     */
    public void transferAccount(String fromAccountName, String toAccountName, BigDecimal amount, String currency) {
        EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            //load the accounts for update
            BankAccount fromAccount = getBankAcount(fromAccountName);
            BankAccount toAccount = getBankAcount(toAccountName);

            if (fromAccount == null || toAccount == null) {
                throw new RuntimeException("bank account can not be null!");
            }

            //check from account balance if it is sufficient or not
            checkAccountBalance(fromAccount, amount, currency);

            CurrencyUnit currencyUnit = Monetary.getCurrency(currency);
            MonetaryAmount fromAccountAmount = Money.of(fromAccount.getBalance(), currencyUnit);
            MonetaryAmount toAccountAmount = Money.of(toAccount.getBalance(), currencyUnit);

            MonetaryAmount toBeTransferedAmount = Money.of(amount, currencyUnit);

            //transfer from account, and subtract the transferred amount
            MonetaryAmount fromAccountAmountAfterTransfer =  fromAccountAmount.subtract(toBeTransferedAmount);
            //update the from account balance
            fromAccount.setBalance(fromAccountAmountAfterTransfer.getNumber().numberValue(BigDecimal.class));

            //add the transferred amount to the to account
            MonetaryAmount toAccountAmountAfterTransfer = toAccountAmount.add(toBeTransferedAmount);
            //update the to account balance
            toAccount.setBalance(toAccountAmountAfterTransfer.getNumber().numberValue(BigDecimal.class));

            //persist from and to accounts to db
            updateBankAccountBalanceWithOptimisticLock(fromAccount);
            updateBankAccountBalanceWithOptimisticLock(toAccount);

            transaction.commit();
        } catch (Exception e) {
            //catch any exception, then roll back
            transaction.rollback();
            throw e;
        }
    }

    /**
     * check account balance
     * @param acount
     * @param amount
     * @param currency
     */
    public void checkAccountBalance(BankAccount acount, BigDecimal amount, String currency) {

        if (acount == null) {
            throw new RuntimeException("bank account can not be null!");
        }
        if (amount ==null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw  new RuntimeException("Transferred amount must be greater than zero!");
        }
        CurrencyUnit currencyUnit = Monetary.getCurrency(currency);
        MonetaryAmount accountAmount = Money.of(acount.getBalance(), currencyUnit);

        MonetaryAmount toBeTransferedAmount = Money.of(amount, currencyUnit);

        if (accountAmount.isLessThan(toBeTransferedAmount)) {
            throw new RuntimeException("bank account" + acount.getName() +  " doesn't has sufficient balance to transfer.");
        }
    }

}