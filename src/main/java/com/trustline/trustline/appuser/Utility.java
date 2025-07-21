package com.trustline.trustline.appuser;

import lombok.experimental.UtilityClass;

import java.util.Random;
@UtilityClass
public class Utility {
    public static int generateSixDigitsNumber(){
        Random random = new Random();
        return 100000 + random.nextInt(900000);
    }
}
