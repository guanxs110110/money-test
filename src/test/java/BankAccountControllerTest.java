//import com.guan.money.service.BankAccountService;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//
//import com.guan.money.application.Application;
//
//import javax.money.CurrencyUnit;
//import javax.money.Monetary;
//import javax.money.MonetaryAmount;
//import java.math.BigDecimal;
//import java.util.concurrent.LinkedBlockingQueue;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = Application.class)
//public class BankAccountControllerTest {
//
//	@Autowired
//	WebApplicationContext wac;
//
//    private MockMvc mockMvc;
//
//	@Autowired
//	private BankAccountService bankAccountService;
//
//    @Before
//    public void setupMockMvc() {
//    	mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
//    }
//
//
//    @Test
//    public void testBankAccountTransferSmallMoney() throws Exception {
//
//    	//transfer 10000 HKD, 10000 hkd less than the default balance 100000, check balance should return success
//    	mockMvc.perform(MockMvcRequestBuilders.get("/checkBalance")
//    			.param("accountName", "TestAcountA")
//    			.param("transferAmount", "10000")
//    			.param("currency", "HKD"))
//    			.andExpect(MockMvcResultMatchers.status().isOk())
//    			.andExpect(MockMvcResultMatchers.jsonPath("$").value("success"));
//
//    	//transfer 10000 HKD, TestAcountA has sufficient balance, expect return success
//    	mockMvc.perform(MockMvcRequestBuilders.get("/transfer")
//    			.param("fromAccountName", "TestAcountA")
//    			.param("toAccountName", "TestAcountB")
//    			.param("amount", "10000")
//    			.param("currency", "HKD"))
//    			.andExpect(MockMvcResultMatchers.status().isOk())
//    			.andExpect(MockMvcResultMatchers.jsonPath("$").value("success"));
//
//    	//after transfer, TestAcountA should have 90000 hkd left
//    	mockMvc.perform(MockMvcRequestBuilders.get("/getBalance")
//    			.param("accountName", "TestAcountA"))
//    			.andExpect(MockMvcResultMatchers.status().isOk())
//    			.andExpect(MockMvcResultMatchers.jsonPath("$.balance").value(90000));
//
//
//    	//after transfer, TestAcountB should have 110000 hkd
//    	mockMvc.perform(MockMvcRequestBuilders.get("/getBalance")
//    			.param("accountName", "TestAcountB"))
//    			.andExpect(MockMvcResultMatchers.status().isOk())
//    			.andExpect(MockMvcResultMatchers.jsonPath("$.balance").value(110000));
//
//    }
//
//    @Test
//    public void testBankAccountTransferBigAmountMoney() throws Exception {
//
//    	//transfer 999999 HKD, 999999 hkd greater than the default balance 100000, check balance should return failed
//    	mockMvc.perform(MockMvcRequestBuilders.get("/checkBalance")
//    			.param("accountName", "TestAcountA")
//    			.param("transferAmount", "999999")
//    			.param("currency", "HKD"))
//    			.andExpect(MockMvcResultMatchers.status().isOk())
//    			.andExpect(MockMvcResultMatchers.jsonPath("$").value("failed"));
//
//    	//transfer 999999 HKD, TestAcountA has insufficient balance, expect return failed
//    	mockMvc.perform(MockMvcRequestBuilders.get("/transfer")
//    			.param("fromAccountName", "TestAcountA")
//    			.param("toAccountName", "TestAcountB")
//    			.param("amount", "999999")
//    			.param("currency", "HKD"))
//    			.andExpect(MockMvcResultMatchers.status().isOk())
//    			.andExpect(MockMvcResultMatchers.jsonPath("$").value("failed"));
//    }
//
//    @Test
//    public void testBankAccountCheckBalance() throws Exception {
//
//        //transfer 0 HKD, check balance expected return failed
//        mockMvc.perform(MockMvcRequestBuilders.get("/checkBalance")
//                        .param("accountName", "TestAcountA")
//                        .param("transferAmount", "0")
//                        .param("currency", "HKD"))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.jsonPath("$").value("failed"));
//
//        //transfer -100 HKD, check balance expected return failed
//        mockMvc.perform(MockMvcRequestBuilders.get("/checkBalance")
//                        .param("accountName", "TestAcountA")
//                        .param("transferAmount", "-100")
//                        .param("currency", "HKD"))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.jsonPath("$").value("failed"));
//    }
//
//	@Test
//	public void testCreateBankAccount() throws Exception {
//
//		//create a new bank account TestAcountC with 100 HKD balance
//		bankAccountService.createBankAccount("TestAcountC", "HKD", new BigDecimal(100));
//
//		//while call getBalance API, TestAcountC exists and should have 100 hkd balance
//		mockMvc.perform(MockMvcRequestBuilders.get("/getBalance")
//						.param("accountName", "TestAcountC"))
//				.andExpect(MockMvcResultMatchers.status().isOk())
//				.andExpect(MockMvcResultMatchers.jsonPath("$.balance").value(100));
//	}
//
//}
