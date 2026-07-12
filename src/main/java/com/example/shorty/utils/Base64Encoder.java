package com.example.shorty.utils;

public class Base64Encoder {
    private static final String ALPHABET= "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE62=62;

    private Base64Encoder(){}

    public static String encode(long value){
      if (value==0) return "0";

      StringBuilder sb = new StringBuilder();
      while (value > 0){
          sb.append(ALPHABET.charAt((int) (value % BASE62)));
          value/=62;
      }
      return sb.reverse().toString();
    }

    public static long decode(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            throw new IllegalArgumentException("encoded must not be empty");
        }
        long res = 0;
        for (int i = 0; i < encoded.length(); i++) {
            int idx = ALPHABET.indexOf(encoded.charAt(i));
            if (idx == -1)
                throw new IllegalArgumentException("Invalid character: " + encoded.charAt(i));
            res = res * 62 + idx;
        }
        return res;
    }
}
