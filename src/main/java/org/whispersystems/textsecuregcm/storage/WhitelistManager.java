package org.whispersystems.textsecuregcm.storage;


import com.google.common.base.Optional;

import java.util.List;

public class WhitelistManager {

  private final Whitelist whitelist;

  public WhitelistManager(Whitelist whitelist) {
    this.whitelist = whitelist;
  }

  public boolean isInWhitelist(String number, long type) {
    return this.whitelist.isInWhitelist(number, type);
  }
}
