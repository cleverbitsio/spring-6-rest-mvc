package guru.springframework.spring6restmvc.controller;

import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;

public class MockMvCHelper {
    static void printResultActions(ResultActions resultActions) throws UnsupportedEncodingException {
        MvcResult mvcResult = resultActions.andReturn();
        System.out.println("mvcResult Response Content: \n" + mvcResult.getResponse().getContentAsString());
        System.out.println("mvcResult Status Code: \n" + mvcResult.getResponse().getStatus());
    }

    static void printMvcResult(MvcResult mvcResult) throws UnsupportedEncodingException {
        System.out.println("mvcResult Response Content: \n" + mvcResult.getResponse().getContentAsString());
        System.out.println("mvcResult Status Code: \n" + mvcResult.getResponse().getStatus());
    }
}
