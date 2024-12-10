# money-test
简单模拟资金转账的服务

一、JDK1.8 + h2 db + money API + JPA,  eclipse构建结构如下：


![image](https://github.com/user-attachments/assets/a40d8a1c-fd40-4161-9b50-300554566f78)

二、项目提供如下restful API，通过运行application可以启动spring boot应用

1、/transfer

转账API

2、/getBalance

获取账号信息和余额

3、/checkBalance

校验余额是否充足

三、启动spring boot项目，会初始化2个账号（TestAcountA与TestAcountB）到H2DB

![image](https://github.com/user-attachments/assets/eaa3b226-eb70-42f0-995a-b03100e5fc01)

四、可以直接通过BankAccountServiceTest进行API单元测试
<img width="697" alt="image" src="https://github.com/user-attachments/assets/50666258-8946-4262-a339-39eda0f5f83a">


单元测试结果正常可以通过.




