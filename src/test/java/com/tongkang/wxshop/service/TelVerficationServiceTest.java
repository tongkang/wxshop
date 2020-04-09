package com.tongkang.wxshop.service;

import com.tongkang.wxshop.controller.AuthController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class TelVerficationServiceTest {

    public static AuthController.TelAndCode VALID_PARAMETER = new AuthController.TelAndCode("15327295959", null);
    public static AuthController.TelAndCode VALID_PARAMETER_CODE = new AuthController.TelAndCode("15327295959", "000000");
    public static AuthController.TelAndCode INVALID_PARAMETER = new AuthController.TelAndCode(null, null);

    @Test
    public void returnTrueIfValid() {
        Assertions.assertTrue(new TelVerficationService().verifyTelParameter(VALID_PARAMETER));
    }

    @Test
    public void returnFalseIfinvalid() {
        Assertions.assertFalse(new TelVerficationService().verifyTelParameter(INVALID_PARAMETER));
        Assertions.assertFalse(new TelVerficationService().verifyTelParameter(null));
    }
}
