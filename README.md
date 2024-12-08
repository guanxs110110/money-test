# money-test
简单模拟资金转账的服务

一、JDK1.8 + h2 db + money API,  eclipse构建结构如下：






![image](https://github.com/user-attachments/assets/a40d8a1c-fd40-4161-9b50-300554566f78)

二、项目提供如下restful API

1、/transfer

转账API

2、/getBalance

获取账号信息和余额

3、/checkBalance

校验余额是否充足

三、启动spring boot项目，会初始化2个账号到H2DB

![image](https://github.com/user-attachments/assets/eaa3b226-eb70-42f0-995a-b03100e5fc01)

四、可以直接通过BankAccountControllerTest进行API单元测试
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.guan.money.application.Application;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class BankAccountControllerTest {
	
	@Autowired
	WebApplicationContext wac;
    
    private MockMvc mockMvc;
    
    @Before
    public void setupMockMvc() {
    	mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }
    
    
    @Test
    public void testBankAccountTransferSmallMoney() throws Exception {
    	
    	//transfer 10000 HKD, 10000 hkd less than the default balance 100000, check balance should return success
    	mockMvc.perform(MockMvcRequestBuilders.get("/checkBalance")
    			.param("accountName", "TestAcountA")
    			.param("transferAmount", "10000")
    			.param("currency", "HKD"))
    			.andExpect(MockMvcResultMatchers.status().isOk())
    			.andExpect(MockMvcResultMatchers.jsonPath("$").value("success"));
    	
    	//transfer 10000 HKD, TestAcountA has sufficient balance, expect return success
    	mockMvc.perform(MockMvcRequestBuilders.get("/transfer")
    			.param("fromAccountName", "TestAcountA")
    			.param("toAccountName", "TestAcountB")
    			.param("amount", "10000")
    			.param("currency", "HKD"))
    			.andExpect(MockMvcResultMatchers.status().isOk())
    			.andExpect(MockMvcResultMatchers.jsonPath("$").value("success"));
    	
    	//after transfer, TestAcountA should have 90000 hkd left
    	mockMvc.perform(MockMvcRequestBuilders.get("/getBalance")
    			.param("accountName", "TestAcountA"))
    			.andExpect(MockMvcResultMatchers.status().isOk())
    			.andExpect(MockMvcResultMatchers.jsonPath("$.balance").value(90000));
    	
    	
    	//after transfer, TestAcountB should have 110000 hkd
    	mockMvc.perform(MockMvcRequestBuilders.get("/getBalance")
    			.param("accountName", "TestAcountB"))
    			.andExpect(MockMvcResultMatchers.status().isOk())
    			.andExpect(MockMvcResultMatchers.jsonPath("$.balance").value(110000));
    	
    }
    
    @Test
    public void testBankAccountTransferBigAmountMoney() throws Exception {
    	
    	//transfer 999999 HKD, 999999 hkd greater than the default balance 100000, check balance should return failed
    	mockMvc.perform(MockMvcRequestBuilders.get("/checkBalance")
    			.param("accountName", "TestAcountA")
    			.param("transferAmount", "999999")
    			.param("currency", "HKD"))
    			.andExpect(MockMvcResultMatchers.status().isOk())
    			.andExpect(MockMvcResultMatchers.jsonPath("$").value("failed"));
    	
    	//transfer 999999 HKD, TestAcountA has insufficient balance, expect return failed
    	mockMvc.perform(MockMvcRequestBuilders.get("/transfer")
    			.param("fromAccountName", "TestAcountA")
    			.param("toAccountName", "TestAcountB")
    			.param("amount", "999999")
    			.param("currency", "HKD"))
    			.andExpect(MockMvcResultMatchers.status().isOk())
    			.andExpect(MockMvcResultMatchers.jsonPath("$").value("failed"));
    }
}

单元测试正常可以正常通过：

![image](https://github.com/user-attachments/assets/fbc2c0c2-d114-4388-bb8f-8cac6dbc40bc)



