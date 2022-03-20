package com.crypto.moneymachine;

import com.binance.api.client.BinanceApiRestClient;
import com.crypto.moneymachine.connection.ConnectionManager;
import com.crypto.moneymachine.controller.BalancesController;
import com.crypto.moneymachine.service.BalancesService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BalancesController.class)
//@RunWith(MockitoJUnitRunner.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class BalancesControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    BalancesService balancesService;
    @MockBean
    ConnectionManager connectionManager;

    @Autowired
    private WebApplicationContext wac;

//    @Before
//    public void setup() {
//        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
//    }

    @Test
    public void shouldShowAllBalances() throws Exception{
//        given(connectionManager.getClient()).willReturn(null);
//        given(balancesService.showAllBalances(any(BinanceApiRestClient.class))).willReturn("testReturn");
        when(connectionManager.getClient()).thenReturn(null);
        when(balancesService.showAllBalances(any(BinanceApiRestClient.class))).thenReturn("testReturn");

        mockMvc.perform(get("/balances/all"))
                .andExpect(status().isOk())
                .andExpect(content().string("testReturn"));
    }
}
