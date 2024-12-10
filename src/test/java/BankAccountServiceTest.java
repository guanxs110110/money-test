import com.guan.money.entity.BankAccount;
import com.guan.money.service.BankAccountService;
import org.javamoney.moneta.Money;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.util.concurrent.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BankAccountService.class)
public class BankAccountServiceTest {

    @Autowired
    private BankAccountService bankAccountService;

    @Test
    public void testCreateBankAccount() {

        try {
            //create a new bank account TestAcountC with 100 HKD balance
            bankAccountService.createBankAccount("TestAcountC", "HKD", new BigDecimal(100));

            //check account "TestAcountC" should exist and have 100 hkd balance
            BankAccount bankAccount = bankAccountService.getBankAcount("TestAcountC");

            Assert.assertNotNull(bankAccount);
            Assert.assertEquals("TestAcountC", bankAccount.getName());
            Assert.assertEquals(new BigDecimal(100), bankAccount.getBalance());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            bankAccountService.closeEntityManager();
        }

    }

    @Test
    public void testGetBankAcount() {

        try {
            //get the init account "TestAcountA",  should exist
            BankAccount bankAccount = bankAccountService.getBankAcount("TestAcountA");

            Assert.assertNotNull(bankAccount);
            Assert.assertEquals("TestAcountA", bankAccount.getName());
        }finally {
            bankAccountService.closeEntityManager();
        }
    }

    @Test
    public void testCheckAccountBalance() {

        try {
            //get the init account "TestAcountA" to do the account balance check before transferring
            BankAccount bankAccount = bankAccountService.getBankAcount("TestAcountA");

            //check transferring -100 HKD, should throw exception, exception is not null
            Exception exception = null;
            try {
                bankAccountService.checkAccountBalance(bankAccount, new BigDecimal(-100), "HKD");
            } catch (Exception ex) {
                exception = ex;
            }
            Assert.assertNotNull(exception);
            Assert.assertEquals("Transferred amount must be greater than zero!", exception.getMessage());

            //check transferring 0 HKD, should also throw exception, exception is not null
            exception = null;
            try {
                bankAccountService.checkAccountBalance(bankAccount, new BigDecimal(0), "HKD");
            } catch (Exception ex) {
                exception = ex;
            }
            Assert.assertNotNull(exception);
            Assert.assertEquals("Transferred amount must be greater than zero!", exception.getMessage());

            //check transferring 10000 HKD, should pass and no exception happens
            exception = null;
            try {
                bankAccountService.checkAccountBalance(bankAccount, new BigDecimal(10000), "HKD");
            } catch (Exception ex) {
                exception = ex;
            }
            Assert.assertNull(exception);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            bankAccountService.closeEntityManager();
        }

    }

    @Test
    public void testTransferAccount() {

        try {
            //transfer 10000 HKD from TestAcountA to TestAcountB, TestAcountA has sufficient balance, expect no exception happen.
            Exception exception = null;
            try {
                bankAccountService.transferAccount("TestAcountA", "TestAcountB", new BigDecimal("10000"),"HKD");
            } catch (Exception ex) {
                exception = ex;
            }
            Assert.assertNull(exception);

            //check account "TestAcountA" should have 90000 hkd balance left
            BankAccount bankAccountA = bankAccountService.getBankAcount("TestAcountA");
            Assert.assertNotNull(bankAccountA);
            Assert.assertEquals(new BigDecimal(90000), bankAccountA.getBalance());

            //check account "TestAcountB" should increase to 110000 hkd balance
            BankAccount bankAccountB = bankAccountService.getBankAcount("TestAcountB");
            Assert.assertNotNull(bankAccountB);
            Assert.assertEquals(new BigDecimal(110000), bankAccountB.getBalance());

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            bankAccountService.closeEntityManager();
        }

    }

    @Test
    @DirtiesContext
    public void testConcurrent() {

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 5; i++) {
            FutureTask<String> futureTask = new FutureTask<>(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    try {
                        bankAccountService.transferAccount("TestAcountA", "TestAcountB", new BigDecimal(100), "HKD");
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    } finally {
                        bankAccountService.closeEntityManager();
                    }

                    return "finished";
                }
            });

            FutureTask<String> futureTask1 = new FutureTask<>(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    try {
                        bankAccountService.transferAccount("TestAcountB", "TestAcountA", new BigDecimal(100), "HKD");
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    } finally {
                        bankAccountService.closeEntityManager();
                    }

                    return "finished";
                }
            });
            executorService.submit(futureTask);
            executorService.submit(futureTask1);

            // 获取任务结果，等待任务完成或超时
            try {
                futureTask.get(); // 等待1秒获取结果，超时则抛出异常
                futureTask1.get();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        try {
            BankAccount bankAccountA = bankAccountService.getBankAcount("TestAcountA");
            BankAccount bankAccountB = bankAccountService.getBankAcount("TestAcountB");

            CurrencyUnit currencyUnit = Monetary.getCurrency("HKD");
            MonetaryAmount accountAmountA = Money.of(bankAccountA.getBalance(), currencyUnit);
            MonetaryAmount accountAmountB = Money.of(bankAccountB.getBalance(), currencyUnit);

            MonetaryAmount actualTotal = accountAmountA.add(accountAmountB);
            MonetaryAmount expectedTotal = Money.of(new BigDecimal(200000), currencyUnit);

            System.out.println(actualTotal);
            System.out.println(expectedTotal);
            Assert.assertEquals(expectedTotal, actualTotal);
        } finally {
            bankAccountService.closeEntityManager();
        }


    }
}
