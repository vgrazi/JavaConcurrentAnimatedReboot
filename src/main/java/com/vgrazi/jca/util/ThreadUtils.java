package com.vgrazi.jca.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThreadUtils {
   private final static Pattern virtualNameMappingPattern = Pattern.compile("(.+])");
   private final static Pattern carrierNameMappingPattern = Pattern.compile(".*@(.+)");
   private final static Pattern noCarrierMappingPattern = Pattern.compile("(.+])/.*");
   public static Map.Entry<String, String> getVirtualToCarrierMapping(Thread thread) {
      String name = thread.toString();
      Matcher matcher = virtualNameMappingPattern.matcher(name);
      String virtualName = "";
      if(matcher.find()) {
         virtualName = matcher.group(1);
      }
      String carrier;
      matcher = carrierNameMappingPattern.matcher(name);
      if(matcher.find()) {
         carrier = matcher.group(1);
      }
      else {
         carrier = "";
      }
      return Map.entry(virtualName, carrier);
   }

}
