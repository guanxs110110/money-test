package com.guan.money.controller;

import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.guan.money.entity.BankAccount;
import com.guan.money.service.BankAccountService;


@RestController
public class BankAccountController {
	
	@Autowired
	BankAccountService bankAccountService;
	
    @RequestMapping("/transfer")
    public String transfer(
            @RequestParam(name = "fromAccountName") String fromAccountName,
            @RequestParam(name = "toAccountName") String toAccountName,
            @RequestParam(name = "amount") BigDecimal amount,
            @RequestParam(name = "currency") String currency,
            HttpServletRequest request) throws Exception {
    	
    	try {
        	bankAccountService.transferAccount(fromAccountName, toAccountName, amount, currency);
    	} catch (Exception ex) {
    		return "failed";
    	} finally {
			bankAccountService.closeEntityManager();
		}
    	
        return "success";
    }
    
    @RequestMapping("/getBalance")
    public String getBalance(
            @RequestParam(name = "accountName") String accountName,
            HttpServletRequest request) throws Exception {
    	
    	BankAccount bankAccount = null;
    	try {
    		bankAccount = bankAccountService.getBankAcount(accountName);
    	} catch (Exception ex) {
    		return null;
    	} finally {
			bankAccountService.closeEntityManager();
		}
    	
        return new Gson().toJson(bankAccount);
    }

    @RequestMapping("/checkBalance")
    public String checkBalance(
            @RequestParam(name = "accountName") String accountName,
            @RequestParam(name = "transferAmount") BigDecimal transferAmount,
            @RequestParam(name = "currency") String currency,
            HttpServletRequest request) throws Exception {
    	
    	BankAccount bankAccount = bankAccountService.getBankAcount(accountName);
    	try {
    		bankAccountService.checkAccountBalance(bankAccount, transferAmount, currency);
    	} catch (Exception ex) {
    		return "failed";
    	} finally {
			bankAccountService.closeEntityManager();
		}
    	
        return "success";
    }
}
